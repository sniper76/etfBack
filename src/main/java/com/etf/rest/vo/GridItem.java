package com.etf.rest.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GridItem {
	private List<Item> grid_data;

//	public List<Item> getGrid_data() {
//		return grid_data;
//	}
//
//	public void setGrid_data(List<Item> grid_data) {
//		this.grid_data = grid_data;
//	}
//
//	@Override
//	public String toString() {
//		return "GridItem [grid_data=" + grid_data + "]";
//	}
	
}
