package com.psj.welfare.Data;


public class FirstCategoryItem {

	int categoryImage;
	int categoryBg;
	String categoryTitle;

	public FirstCategoryItem() {

	}

	public FirstCategoryItem(String categoryTitle) {

		this.categoryTitle = categoryTitle;
	}

	public FirstCategoryItem(int categoryBg) {
		this.categoryBg = categoryBg;
	}

	public FirstCategoryItem(String categoryTitle, int categoryBg) {

		this.categoryTitle = categoryTitle;
		this.categoryBg = categoryBg;
	}

	public void setCategoryImage(int categoryImage) {
		this.categoryImage = categoryImage;
	}

	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}

	public int getCategoryImage() {
		return categoryImage;
	}

	public String getCategoryTitle() {
		return categoryTitle;
	}

	public int getCategoryBg() {
		return categoryBg;
	}

	public void setCategoryBg(int categoryBg) {
		this.categoryBg = categoryBg;
	}
}
