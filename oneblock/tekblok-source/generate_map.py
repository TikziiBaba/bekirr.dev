import struct
import zlib
import gzip
import os

# NBT Encoder helper
def tag_byte(name, val):
    return b'\x01' + encode_str(name) + struct.pack('>b', val)

def tag_short(name, val):
    return b'\x02' + encode_str(name) + struct.pack('>h', val)

def tag_int(name, val):
    return b'\x03' + encode_str(name) + struct.pack('>i', val)

def tag_long(name, val):
    return b'\x04' + encode_str(name) + struct.pack('>q', val)

def tag_float(name, val):
    return b'\x05' + encode_str(name) + struct.pack('>f', val)

def tag_double(name, val):
    return b'\x06' + encode_str(name) + struct.pack('>d', val)

def tag_byte_array(name, data):
    return b'\x07' + encode_str(name) + struct.pack('>i', len(data)) + data

def tag_string(name, val):
    b = val.encode('utf-8')
    return b'\x08' + encode_str(name) + struct.pack('>H', len(b)) + b

def encode_str(s):
    b = s.encode('utf-8')
    return struct.pack('>H', len(b)) + b

def tag_int_array(name, arr):
    data = struct.pack('>' + 'i' * len(arr), *arr)
    return b'\x0b' + encode_str(name) + struct.pack('>i', len(arr)) + data

def tag_long_array(name, arr):
    data = struct.pack('>' + 'q' * len(arr), *arr)
    return b'\x0c' + encode_str(name) + struct.pack('>i', len(arr)) + data

def tag_list(name, tag_type, items):
    out = b'\x09' + encode_str(name) + struct.pack('>b', tag_type) + struct.pack('>i', len(items))
    for it in items:
        out += it
    return out

def tag_compound(name, contents):
    return b'\x0a' + encode_str(name) + contents + b'\x00'

def make_chunk_nbt():
    # Section 4 (Y 64 to 79) contains the grass block at X=0, Y=65 (local Y=1), Z=0
    # In 1.16, BlockStates palette:
    # 0 = minecraft:air
    # 1 = minecraft:grass_block
    palette_air = tag_string("Name", "minecraft:air") + b'\x00'
    palette_grass = tag_string("Name", "minecraft:grass_block") + b'\x00'

    # In 1.16, palette of size 2 uses 4 bits per block (minimum 4 bits)
    # 64-bit longs: 64 / 4 = 16 blocks per long.
    # 4096 blocks / 16 = 256 longs.
    # Block index = (y * 16 + z) * 16 + x.
    # For local X=0, Y=1, Z=0: index = (1 * 16 + 0) * 16 + 0 = 256.
    # 256 / 16 = long index 16. Local block offset = 0.
    # So in long[16], bit 0..3 is 1 (grass_block). All others 0.
    block_states = [0] * 256
    block_states[16] = 1 # index 256 has palette index 1

    sec4 = (
        tag_byte("Y", 4) +
        tag_list("Palette", 10, [palette_air, palette_grass]) +
        tag_long_array("BlockStates", block_states) +
        tag_byte_array("SkyLight", bytes([255] * 2048)) +
        tag_byte_array("BlockLight", bytes([0] * 2048)) +
        b'\x00'
    )

    sections = [sec4]

    # Heightmaps (MOTION_BLOCKING, WORLD_SURFACE)
    # 256 heights of value 66 at (0,0) and 0 elsewhere
    # 9 bits per height: 64 / 9 = 7 values per long. 256 / 7 = 37 longs.
    heights = [0] * 37
    heights[0] = 66 # height of block + 1

    heightmaps = (
        tag_long_array("MOTION_BLOCKING", heights) +
        tag_long_array("WORLD_SURFACE", heights) +
        b'\x00'
    )

    biomes = [1] * 1024 # 1 = minecraft:plains

    level = (
        tag_int("xPos", 0) +
        tag_int("zPos", 0) +
        tag_string("Status", "full") +
        tag_long("LastUpdate", 1000) +
        tag_long("InhabitedTime", 0) +
        tag_list("Sections", 10, sections) +
        tag_int_array("Biomes", biomes) +
        tag_compound("Heightmaps", heightmaps) +
        tag_list("Entities", 10, []) +
        tag_list("TileEntities", 10, []) +
        tag_list("TileTicks", 10, []) +
        tag_list("LiquidTicks", 10, []) +
        tag_list("PostProcessing", 9, [b'\x00\x00\x00\x00'] * 16) +
        b'\x00'
    )

    root = (
        tag_int("DataVersion", 2567) +
        tag_compound("Level", level)
    )

    return tag_compound("", root)

def create_region_file():
    chunk_nbt = make_chunk_nbt()
    compressed = zlib.compress(chunk_nbt)

    # Chunk header: 4 bytes length (compressed len + 1), 1 byte compression type (2 = zlib)
    chunk_data = struct.pack('>IB', len(compressed) + 1, 2) + compressed

    # Calculate sectors (each sector is 4096 bytes)
    sectors = (len(chunk_data) + 4095) // 4096
    padded_chunk = chunk_data.ljust(sectors * 4096, b'\x00')

    # Sector offset: sector 2 (first sector after 8192 bytes header)
    sector_offset = 2
    offset_bytes = struct.pack('>I', (sector_offset << 8) | sectors)[1:] # 3 bytes offset, 1 byte sector count

    # Build 8192-byte header (4096 bytes location table + 4096 bytes timestamps)
    location_table = bytearray(4096)
    # Chunk (0, 0) is at index 0 in location table
    location_table[0:4] = offset_bytes + bytes([sectors])

    timestamp_table = bytearray(4096)
    timestamp_table[0:4] = struct.pack('>I', 1600000000)

    region_path = os.path.join("TekBlokWorld", "region", "r.0.0.mca")
    with open(region_path, "wb") as f:
        f.write(location_table)
        f.write(timestamp_table)
        f.write(padded_chunk)

    print(f"Region dosyası başarıyla yazıldı: {region_path} (Boyut: {os.path.getsize(region_path)} bayt)")

if __name__ == "__main__":
    create_region_file()
