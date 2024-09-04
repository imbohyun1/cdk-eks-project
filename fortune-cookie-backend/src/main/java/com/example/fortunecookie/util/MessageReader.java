package com.example.fortunecookie.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MessageReader {

    public List<String> readMessages(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}