package dev.bekirr.tekblok.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class MapGenerator {

    public static void main(String[] args) {
        File worldDir = new File("TekBlokWorld");
        worldDir.mkdirs();

        File regionDir = new File(worldDir, "region");
        regionDir.mkdirs();

        File levelDat = new File(worldDir, "level.dat");

        try (FileOutputStream fos = new FileOutputStream(levelDat);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             DataOutputStream out = new DataOutputStream(gzos)) {

            // Root compound
            out.writeByte(10); // TagCompound
            writeString(out, ""); // Root name is empty string

            // Data compound
            out.writeByte(10);
            writeString(out, "Data");

            // DataVersion (2567 = 1.16.1)
            writeInt(out, "DataVersion", 2567);

            // LevelName
            writeStringTag(out, "LevelName", "TekBlokWorld");

            // generatorName
            writeStringTag(out, "generatorName", "flat");

            // generatorVersion
            writeInt(out, "generatorVersion", 1);

            // Spawn coordinates
            writeInt(out, "SpawnX", 0);
            writeInt(out, "SpawnY", 66);
            writeInt(out, "SpawnZ", 0);

            // GameType (0 = survival)
            writeInt(out, "GameType", 0);

            // Difficulty (1 = easy, 2 = normal)
            writeByte(out, "Difficulty", (byte) 2);
            writeByte(out, "DifficultyLocked", (byte) 0);

            // Time & Weather
            writeLong(out, "Time", 6000L);
            writeLong(out, "DayTime", 6000L);
            writeInt(out, "clearWeatherTime", 999999);
            writeInt(out, "rainTime", 0);
            writeByte(out, "raining", (byte) 0);
            writeInt(out, "thunderTime", 0);
            writeByte(out, "thundering", (byte) 0);

            // Flags
            writeByte(out, "hardcore", (byte) 0);
            writeByte(out, "allowCommands", (byte) 1);
            writeByte(out, "initialized", (byte) 1);
            writeInt(out, "version", 19133);

            // Version compound
            out.writeByte(10);
            writeString(out, "Version");
            writeInt(out, "Id", 2567);
            writeStringTag(out, "Name", "1.16.1");
            writeByte(out, "Snapshot", (byte) 0);
            out.writeByte(0); // End Version

            // GameRules compound
            out.writeByte(10);
            writeString(out, "GameRules");
            writeStringTag(out, "doMobSpawning", "true");
            writeStringTag(out, "doDaylightCycle", "true");
            writeStringTag(out, "doWeatherCycle", "false");
            writeStringTag(out, "spawnRadius", "0");
            writeStringTag(out, "keepInventory", "false");
            out.writeByte(0); // End GameRules

            out.writeByte(0); // End Data
            out.writeByte(0); // End Root

            out.flush();
            System.out.println("TekBlokWorld/level.dat başarıyla oluşturuldu!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeString(DataOutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    private static void writeByte(DataOutputStream out, String name, byte val) throws IOException {
        out.writeByte(1);
        writeString(out, name);
        out.writeByte(val);
    }

    private static void writeInt(DataOutputStream out, String name, int val) throws IOException {
        out.writeByte(3);
        writeString(out, name);
        out.writeInt(val);
    }

    private static void writeLong(DataOutputStream out, String name, long val) throws IOException {
        out.writeByte(4);
        writeString(out, name);
        out.writeLong(val);
    }

    private static void writeStringTag(DataOutputStream out, String name, String val) throws IOException {
        out.writeByte(8);
        writeString(out, name);
        writeString(out, val);
    }
}
