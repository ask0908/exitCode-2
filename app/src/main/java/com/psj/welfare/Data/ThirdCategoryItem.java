package com.psj.welfare.Data;

public class ThirdCategoryItem {

	int categoryBg;
	String categoryTitle;

	public ThirdCategoryItem() {

	}

	public ThirdCategoryItem(String categoryTitle, int categoryBg) {

		this.categoryTitle = categoryTitle;
		this.categoryBg = categoryBg;
	}

	public void setCategoryImage(int categoryBg) {
		this.categoryBg = categoryBg;
	}

	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}

	public int getcategoryBg() {
		return categoryBg;
	}

	public String getCategoryTitle() {
		return categoryTitle;
	}
}
