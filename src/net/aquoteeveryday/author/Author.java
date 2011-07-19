package net.aquoteeveryday.author;

public class Author {
	private Integer id;
	private String name;
	private String wikipedia;
	
	public Author(Integer id, String name, String wikipedia) {
		setId(id);
		setName(name);
		setWikipedia(wikipedia);
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setWikipedia(String wikipedia) {
		this.wikipedia = wikipedia;
	}
	public String getWikipedia() {
		return wikipedia;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
