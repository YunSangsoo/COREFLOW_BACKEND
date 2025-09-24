package com.kh.coreflow.humanmanagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrganizationDto {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ParentDep{
		private Long depId;
		private String depName;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChildDep{
		private Long depId;
		private String depName;
		private Integer parentId;
	}
}