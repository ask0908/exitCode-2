package com.psj.welfare.Data;

public class EndCategoryItem {

	private String categoryTitle;
	private int categoryBg;

	public EndCategoryItem() {

	}

	public EndCategoryItem(String categoryTitle, int categoryBg) {
		this.categoryTitle = categoryTitle;
		this.categoryBg = categoryBg;
	}

	public void setCategoryBg(int categoryBg) {
		this.categoryBg = categoryBg;
	}

	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}

	public int getCategoryBg() {
		return categoryBg;
	}

	public String getCategoryTitle() {
		return categoryTitle;
	}
}
