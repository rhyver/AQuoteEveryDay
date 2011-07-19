package net.aquoteeveryday.quote;

import java.util.Date;

import net.aquoteeveryday.author.Author;

public class Quote {
	private Integer id;
	private String text;
	private Author autor;
	private String language;
	private Date date;
	
	public Quote(String text,
				 Author author,
				 String language,
				 Date date) {
		
		setText(text);
		setAutor(author);
		setLanguage(language);
		setDate(date);
	}
	
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Date getDate() {
		return date;
	}

	public void setAutor(Author autor) {
		this.autor = autor;
	}

	public Author getAutor() {
		return autor;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
	
	
}
