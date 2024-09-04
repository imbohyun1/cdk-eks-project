package com.example.fortunecookie.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "fortunes")
public class Fortune {
    @Id
    private String id;
    private String message;
    private List<Integer> luckyNumbers;

    public Fortune() {}

    public Fortune(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Integer> getLuckyNumbers() {
        return luckyNumbers;
    }

    public void setLuckyNumbers(List<Integer> luckyNumbers) {
        this.luckyNumbers = luckyNumbers;
    }

    @Override
    public String toString() {
        return "Fortune{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", luckyNumbers=" + luckyNumbers +
                '}';
    }
}