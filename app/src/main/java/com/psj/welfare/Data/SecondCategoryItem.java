package com.psj.welfare.Data;

public class SecondCategoryItem {

	int categoryBg;
	String categoryTitle;

	public SecondCategoryItem() {

	}

	public SecondCategoryItem(String categoryTitle, int categoryBg) {

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
