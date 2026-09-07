import os
import sys
import glob
import struct
import zlib
import io
import gzip
import shutil
import time

BLOCK_MAP = {
    'minecraft:bamboo_planks': 'minecraft:birch_planks',
    'minecraft:bamboo_slab': 'minecraft:birch_slab',
    'minecraft:bamboo_stairs': 'minecraft:birch_stairs',
    'minecraft:bamboo_trapdoor': 'minecraft:birch_trapdoor',
    'minecraft:bamboo_fence': 'minecraft:birch_fence',
    'minecraft:bamboo_fence_gate': 'minecraft:birch_fence_gate',
    'minecraft:chiseled_bookshelf': 'minecraft:bookshelf',
    'minecraft:decorated_pot': 'minecraft:flower_pot',
    'minecraft:pink_petals': 'minecraft:oxeye_daisy',
    'minecraft:torchflower': 'minecraft:poppy',
    'minecraft:oak_hanging_sign': 'minecraft:oak_wall_sign',
    'minecraft:spruce_hanging_sign': 'minecraft:spruce_wall_sign',
    'minecraft:birch_hanging_sign': 'minecraft:birch_wall_sign',
    'minecraft:jungle_hanging_sign': 'minecraft:jungle_wall_sign',
    'minecraft:acacia_hanging_sign': 'minecraft:acacia_wall_sign',
    'minecraft:dark_oak_hanging_sign': 'minecraft:dark_oak_wall_sign',
    'minecraft:crimson_hanging_sign': 'minecraft:crimson_wall_sign',
    'minecraft:warped_hanging_sign': 'minecraft:warped_wall_sign',
    'minecraft:deepslate': 'minecraft:blackstone',
    'minecraft:polished_deepslate': 'minecraft:polished_blackstone',
    'minecraft:deepslate_bricks': 'minecraft:stone_bricks',
    'minecraft:cracked_deepslate_bricks': 'minecraft:cracked_stone_bricks',
    'minecraft:deepslate_brick_slab': 'minecraft:stone_brick_slab',
    'minecraft:deepslate_brick_stairs': 'minecraft:stone_brick_stairs',
    'minecraft:deepslate_brick_wall': 'minecraft:stone_brick_wall',
    'minecraft:deepslate_tiles': 'minecraft:stone_bricks',
    'minecraft:deepslate_tile_slab': 'minecraft:stone_brick_slab',
    'minecraft:deepslate_tile_stairs': 'minecraft:stone_brick_stairs',
    'minecraft:cobbled_deepslate': 'minecraft:cobblestone',
    'minecraft:cobbled_deepslate_slab': 'minecraft:cobblestone_slab',
    'minecraft:cobbled_deepslate_stairs': 'minecraft:cobblestone_stairs',
    'minecraft:cobbled_deepslate_wall': 'minecraft:cobblestone_wall',
    'minecraft:mangrove_leaves': 'minecraft:oak_leaves',
    'minecraft:mangrove_roots': 'minecraft:oak_wood',
    'minecraft:packed_mud': 'minecraft:terracotta',
    'minecraft:mud_bricks': 'minecraft:stone_bricks',
    'minecraft:mud_brick_slab': 'minecraft:stone_brick_slab',
    'minecraft:mud_brick_stairs': 'minecraft:stone_brick_stairs',
    'minecraft:lightning_rod': 'minecraft:iron_bars',
    'minecraft:spore_blossom': 'minecraft:allium',
    'minecraft:rooted_dirt': 'minecraft:coarse_dirt',
    'minecraft:short_grass': 'minecraft:grass',
    'minecraft:water_cauldron': 'minecraft:cauldron',
    'minecraft:iron_chain': 'minecraft:chain',
    'minecraft:copper_block': 'minecraft:iron_block',
    'minecraft:cut_copper': 'minecraft:stone',
    'minecraft:raw_iron_block': 'minecraft:iron_block',
    'minecraft:raw_gold_block': 'minecraft:gold_block',
    'minecraft:raw_copper_block': 'minecraft:iron_block',
    'minecraft:amethyst_block': 'minecraft:purpur_block',
    'minecraft:calcite': 'minecraft:diorite',
    'minecraft:dripstone_block': 'minecraft:granite',
    'minecraft:tinted_glass': 'minecraft:black_stained_glass',
    'minecraft:moss_block': 'minecraft:grass_block',
    'minecraft:moss_carpet': 'minecraft:green_carpet',
    'minecraft:glow_lichen': 'minecraft:vine',
    'minecraft:candle': 'minecraft:torch',
    'minecraft:white_candle': 'minecraft:torch',
}

def parse_nbt(f):
    tag_type = f.read(1)
    if not tag_type or tag_type == b'\x00': return 0, '', None
    tt = tag_type[0]
    name_len = struct.unpack('>H', f.read(2))[0]
    name = f.read(name_len).decode('utf-8', 'replace')
    val = parse_payload(f, tt)
    return tt, name, val

def parse_payload(f, tt):
    if tt == 1: return struct.unpack('>b', f.read(1))[0]
    elif tt == 2: return struct.unpack('>h', f.read(2))[0]
    elif tt == 3: return struct.unpack('>i', f.read(4))[0]
    elif tt == 4: return struct.unpack('>q', f.read(8))[0]
    elif tt == 5: return struct.unpack('>f', f.read(4))[0]
    elif tt == 6: return struct.unpack('>d', f.read(8))[0]
    elif tt == 7:
        length = struct.unpack('>i', f.read(4))[0]
        return f.read(length)
    elif tt == 8:
        length = struct.unpack('>H', f.read(2))[0]
        return f.read(length).decode('utf-8', 'replace')
    elif tt == 9:
        sub_type = f.read(1)[0]
        length = struct.unpack('>i', f.read(4))[0]
        return (sub_type, [parse_payload(f, sub_type) for _ in range(length)])
    elif tt == 10:
        res = {}
        while True:
            st = f.read(1)
            if not st or st == b'\x00': break
            s_type = st[0]
            s_namelen = struct.unpack('>H', f.read(2))[0]
            s_name = f.read(s_namelen).decode('utf-8', 'replace')
            res[s_name] = (s_type, parse_payload(f, s_type))
        return res
    elif tt == 11:
        length = struct.unpack('>i', f.read(4))[0]
        return struct.unpack('>' + 'i'*length, f.read(4*length))
    elif tt == 12:
        length = struct.unpack('>i', f.read(4))[0]
        return struct.unpack('>' + 'q'*length, f.read(8*length))
    return None

def write_nbt(tt, name, val, out):
    if tt == 0:
        out.write(b'\x00')
        return
    out.write(bytes([tt]))
    nb = name.encode('utf-8')
    out.write(struct.pack('>H', len(nb)) + nb)
    write_payload(tt, val, out)

def write_payload(tt, val, out):
    if tt == 1: out.write(struct.pack('>b', val))
    elif tt == 2: out.write(struct.pack('>h', val))
    elif tt == 3: out.write(struct.pack('>i', val))
    elif tt == 4: out.write(struct.pack('>q', val))
    elif tt == 5: out.write(struct.pack('>f', val))
    elif tt == 6: out.write(struct.pack('>d', val))
    elif tt == 7:
        out.write(struct.pack('>i', len(val)) + val)
    elif tt == 8:
        vb = val.encode('utf-8')
        out.write(struct.pack('>H', len(vb)) + vb)
    elif tt == 9:
        sub_type, items = val
        out.write(bytes([sub_type]))
        out.write(struct.pack('>i', len(items)))
        for item in items:
            write_payload(sub_type, item, out)
    elif tt == 10:
        for k, (s_type, s_val) in val.items():
            write_nbt(s_type, k, s_val, out)
        out.write(b'\x00')
    elif tt == 11:
        out.write(struct.pack('>i', len(val)))
        out.write(struct.pack('>' + 'i'*len(val), *val))
    elif tt == 12:
        out.write(struct.pack('>i', len(val)))
        out.write(struct.pack('>' + 'q'*len(val), *val))

def convert_chunk_121_to_116(chunk_nbt, target_cx, target_cz):
    inhabited = chunk_nbt.get('InhabitedTime', (4, 0))[1]
    last_update = chunk_nbt.get('LastUpdate', (4, 0))[1]
    
    new_sections = []
    if 'sections' in chunk_nbt:
        old_sections = chunk_nbt['sections'][1][1]
        for sec in old_sections:
            y_old = sec.get('Y', (1, 0))[1]
            y_new = y_old + 4  # Shift Y by 4 sections (+64 blocks)
            if y_new < 0 or y_new > 15:
                continue
            
            sec_dict = {'Y': (1, y_new)}
            if 'block_states' in sec:
                bs = sec['block_states'][1]
                if 'palette' in bs:
                    old_pal = bs['palette'][1][1]
                    new_pal = []
                    for entry in old_pal:
                        entry_copy = {}
                        for ek, (et, ev) in entry.items():
                            if ek == 'Name':
                                entry_copy['Name'] = (8, BLOCK_MAP.get(ev, ev))
                            else:
                                entry_copy[ek] = (et, ev)
                        new_pal.append(entry_copy)
                    sec_dict['Palette'] = (9, (10, new_pal))
                    if 'data' in bs:
                        sec_dict['BlockStates'] = (12, bs['data'][1])
            new_sections.append(sec_dict)
    
    new_tiles = []
    IGNORED_BES = {'minecraft:decorated_pot', 'minecraft:chiseled_bookshelf', 'minecraft:brushable_block', 'minecraft:calibrated_sculk_sensor', 'minecraft:crafter', 'minecraft:vault'}
    if 'block_entities' in chunk_nbt:
        old_bes = chunk_nbt['block_entities'][1][1]
        for be in old_bes:
            be_id = be.get('id', (8, ''))[1]
            if be_id in IGNORED_BES:
                continue
            be_copy = {}
            for bk, (bt, bv) in be.items():
                if bk == 'y':
                    be_copy['y'] = (3, bv + 64)
                elif bk == 'id':
                    tile_id = bv
                    if 'sign' in tile_id.lower():
                        tile_id = 'minecraft:sign'
                    be_copy['id'] = (bt, tile_id)
                elif bk == 'front_text':
                    messages = bv.get('messages', (9, (8, [])))[1][1]
                    for idx, msg in enumerate(messages[:4]):
                        be_copy[f'Text{idx+1}'] = (8, msg)
                else:
                    be_copy[bk] = (bt, bv)
            new_tiles.append(be_copy)
            
    level = {
        'xPos': (3, target_cx),
        'zPos': (3, target_cz),
        'Status': (8, 'full'),
        'InhabitedTime': (4, inhabited),
        'LastUpdate': (4, last_update),
        'isLightOn': (1, 0),
        'Sections': (9, (10, new_sections)),
        'TileEntities': (9, (10, new_tiles)),
        'Entities': (9, (10, [])),
        'Biomes': (11, [1] * 1024),
        'Structures': (10, {'Starts': (10, {}), 'References': (10, {})}),
    }
    
    return {
        'DataVersion': (3, 2567),
        'Level': (10, level)
    }

def convert_region_file(mca_in_path, mca_out_path, reg_x, reg_z):
    with open(mca_in_path, 'rb') as f:
        header = f.read(4096)
        timestamps = f.read(4096)
        
        chunks_converted = {}
        for rz in range(32):
            for rx in range(32):
                idx = rx + rz * 32
                loc_bytes = header[idx*4:idx*4+4]
                offset = struct.unpack('>I', b'\x00' + loc_bytes[:3])[0]
                if offset != 0:
                    f.seek(offset * 4096)
                    length = struct.unpack('>I', f.read(4))[0]
                    compression = f.read(1)[0]
                    compressed = f.read(length - 1)
                    raw = zlib.decompress(compressed)
                    
                    target_cx = reg_x * 32 + rx
                    target_cz = reg_z * 32 + rz
                    
                    _, _, chunk_nbt = parse_nbt(io.BytesIO(raw))
                    new_chunk = convert_chunk_121_to_116(chunk_nbt, target_cx, target_cz)
                    
                    buf = io.BytesIO()
                    write_nbt(10, '', new_chunk, buf)
                    new_raw = buf.getvalue()
                    new_comp = zlib.compress(new_raw)
                    chunks_converted[(rx, rz)] = new_comp
    
    if not chunks_converted:
        return 0
    
    # Write new MCA
    locations = bytearray(4096)
    current_sector = 2
    sectors_data = bytearray()
    
    for rz in range(32):
        for rx in range(32):
            idx = rx + rz * 32
            if (rx, rz) in chunks_converted:
                comp_data = chunks_converted[(rx, rz)]
                chunk_payload = struct.pack('>IB', len(comp_data) + 1, 2) + comp_data
                sector_count = (len(chunk_payload) + 4095) // 4096
                pad_len = sector_count * 4096 - len(chunk_payload)
                chunk_payload += b'\x00' * pad_len
                
                loc_val = (current_sector << 8) | sector_count
                locations[idx*4:(idx+1)*4] = struct.pack('>I', loc_val)
                
                sectors_data += chunk_payload
                current_sector += sector_count
                
    with open(mca_out_path, 'wb') as f:
        f.write(locations)
        f.write(timestamps)
        f.write(sectors_data)
        
    return len(chunks_converted)

def generate_116_level_dat(source_world_dat, target_spawn_dat):
    with gzip.open(source_world_dat, 'rb') as f:
        tt, name, world_nbt = parse_nbt(f)
        
    data = world_nbt['Data'][1]
    data['LevelName'] = (8, 'spawn')
    data['SpawnX'] = (3, -176)
    data['SpawnY'] = (3, 38)
    data['SpawnZ'] = (3, 256)
    data['clearWeatherTime'] = (3, 1000000)
    data['raining'] = (1, 0)
    data['thundering'] = (1, 0)
    data['rainTime'] = (3, 0)
    data['thunderTime'] = (3, 0)
    
    if 'GameRules' in data:
        gr = data['GameRules'][1]
        gr['doDaylightCycle'] = (8, 'false')
        gr['doWeatherCycle'] = (8, 'false')
        gr['doMobSpawning'] = (8, 'false')
        gr['mobGriefing'] = (8, 'false')
        
    buf = io.BytesIO()
    write_nbt(tt, name, world_nbt, buf)
    compressed = gzip.compress(buf.getvalue())
    
    with open(target_spawn_dat, 'wb') as f:
        f.write(compressed)

def main():
    print('Starting spawn world conversion to Minecraft 1.16.1...')
    source_backup_region = r'c:\Users\dedyu\Desktop\tekblok\spawn_backup\region'
    tekblok_spawn = r'c:\Users\dedyu\Desktop\tekblok\spawn'
    oneblock_spawn = r'c:\Users\dedyu\Desktop\oneblock\spawn'
    world_level_dat = r'c:\Users\dedyu\Desktop\oneblock\world\level.dat'
    
    temp_region = r'c:\Users\dedyu\Desktop\tekblok\temp_converted_region'
    if os.path.exists(temp_region):
        shutil.rmtree(temp_region)
    os.makedirs(temp_region, exist_ok=True)
    
    mca_files = glob.glob(os.path.join(source_backup_region, '*.mca'))
    print(f'Found {len(mca_files)} MCA files to convert from backup source.')
    
    total_chunks = 0
    t0 = time.time()
    for idx, mca in enumerate(mca_files, 1):
        fname = os.path.basename(mca)
        # Parse reg_x and reg_z from r.X.Z.mca
        parts = fname.split('.')
        reg_x = int(parts[1])
        reg_z = int(parts[2])
        
        out_mca = os.path.join(temp_region, fname)
        c = convert_region_file(mca, out_mca, reg_x, reg_z)
        total_chunks += c
        print(f'[{idx}/{len(mca_files)}] Converted {fname} (reg {reg_x}, {reg_z}): {c} chunks')
        
    t1 = time.time()
    print(f'Converted {total_chunks} chunks in {t1 - t0:.2f} seconds.')
    
    # Generate clean level.dat
    print('Generating clean 1.16.1 level.dat...')
    tekblok_level_dat = os.path.join(tekblok_spawn, 'level.dat')
    generate_116_level_dat(world_level_dat, tekblok_level_dat)
    
    # Replace region folder in tekblok\spawn
    tekblok_region = os.path.join(tekblok_spawn, 'region')
    if os.path.exists(tekblok_region):
        shutil.rmtree(tekblok_region)
    shutil.move(temp_region, tekblok_region)
    
    # Clean up 1.21-incompatible leftovers
    poi_dir = os.path.join(tekblok_spawn, 'poi')
    if os.path.exists(poi_dir):
        shutil.rmtree(poi_dir)
        
    chunks_dat = os.path.join(tekblok_spawn, 'data', 'chunks.dat')
    if os.path.exists(chunks_dat):
        os.remove(chunks_dat)
        
    level_dat_old = os.path.join(tekblok_spawn, 'level.dat_old')
    if os.path.exists(level_dat_old):
        os.remove(level_dat_old)
        
    # Copy converted spawn to oneblock\spawn
    print(f'Syncing converted spawn world to {oneblock_spawn}...')
    if os.path.exists(oneblock_spawn):
        shutil.rmtree(oneblock_spawn)
    shutil.copytree(tekblok_spawn, oneblock_spawn)
    
    print('SUCCESS! Spawn world is now 100% Minecraft 1.16.1 compatible.')

if __name__ == '__main__':
    main()
