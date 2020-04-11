package com.atguigu.gmall.search.bean;

import java.math.BigDecimal;
import java.util.List;

public class Movie {
    private String id;
    private String name;
    private BigDecimal doubanScore;
    private List<Actor> actorList;

    public Movie(){}

    public Movie(String id, String name, BigDecimal doubanScore, List<Actor> actorList) {
        this.id = id;
        this.name = name;
        this.doubanScore = doubanScore;
        this.actorList = actorList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDoubanScore() {
        return doubanScore;
    }

    public void setDoubanScore(BigDecimal doubanScore) {
        this.doubanScore = doubanScore;
    }

    public List<Actor> getActorList() {
        return actorList;
    }

    public void setActorList(List<Actor> actorList) {
        this.actorList = actorList;
    }

}
