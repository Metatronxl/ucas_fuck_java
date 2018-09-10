package com.xulei.ucas;

/**
 * @author lei.X
 * @date 2018/9/10
 */
public interface Constants {

    String LOGIN_PAGE = "http://sep.ucas.ac.cn";
    String LOGIN_URL = LOGIN_PAGE + "/slogin";
    String COURSE_SYSTEM = LOGIN_PAGE + "/portal/site/226/821";
    String COURSE_BASE = "http://jwxk.ucas.ac.cn";
    String COURSE_IDENTIFY = COURSE_BASE + "/login?Identity=";
    String COURSE_SELECTED = COURSE_BASE + "/courseManage/selectedCourse";
    String COURSE_SELECTION_BASE = COURSE_BASE + "/courseManage/main";
    String COURSE_CATEGORY = COURSE_BASE + "/courseManage/selectCourse?s=";
    String COURSE_SAVE = COURSE_BASE + "/courseManage/saveCourse?s=";
    String STUDENT_COURSE_EVALUATE_URL ="http://jwjz.ucas.ac.cn/Student/DeskTopModules/";
    String SELECT_COURSE_URL = "http://jwjz.ucas.ac.cn/Student/DesktopModules/Course/SelectCourse.aspx";

}
