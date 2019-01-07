package com.wisebox.gyb;

public class RootClassInfo {
	private String moduleId;
	private String moduleName;
	private String courseCount;
	private String trainDate;
	
	public RootClassInfo()
    {
    }
	
	public RootClassInfo(String id, String name, String count)
    {
        super();
        this.moduleId = id;
        this.moduleName = name;
        this.setCourseCount(count);
    }
	
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getCourseCount() {
		return courseCount;
	}

	public void setCourseCount(String courseCount) {
		this.courseCount = courseCount;
	}

	public String getTrainDate() {
		return trainDate;
	}

	public void setTrainDate(String trainDate) {
		this.trainDate = trainDate;
	}
	
}
