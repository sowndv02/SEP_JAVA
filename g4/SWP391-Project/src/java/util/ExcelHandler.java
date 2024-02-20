/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import dal.ProjectDAO;
import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.ClassStudent;
import model.Project;

public class ExcelHandler {

    public boolean exportClassStudentListToExcel(List<ClassStudent> students, String className) {
        String filePath = "C:\\Users\\ADMIN\\Downloads\\" + className + ".xlsx";

        File file = new File(filePath);

        // Check if the file already exists
        if (file.exists()) {
            // If it exists, delete the existing file
            if (file.delete()) {
                System.out.println("Existing file deleted: " + filePath);
            } else {
                System.err.println("Failed to delete existing file: " + filePath);
                return false; // Return false if the existing file can't be deleted
            }
        }

        try ( Workbook workbook = new XSSFWorkbook()) {
            // ... (rest of your code to create and write the Excel file)

            try ( FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true; // Return true when file export is successful
        } catch (IOException e) {
            e.printStackTrace();  // Log the exception
            return false; // Return false when file export fails
        }
    }

    public boolean exportClassProjectListToExcel(List<Project> projects, String className) {
        String filePath = "C:\\Users\\TRAN DUNG\\Downloads\\" + className + ".xlsx";

        try ( Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Projects");

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("project_id");
            headerRow.createCell(1).setCellValue("project_code");
            headerRow.createCell(2).setCellValue("project_en_name");
            headerRow.createCell(3).setCellValue("project_vi_name");
            headerRow.createCell(4).setCellValue("project_descript");
            headerRow.createCell(5).setCellValue("status");
            headerRow.createCell(6).setCellValue("class_id");
            headerRow.createCell(7).setCellValue("group_name");
            headerRow.createCell(8).setCellValue("mentor_id");
            headerRow.createCell(9).setCellValue("co_mentor_id");

            // Data rows
            for (int i = 0; i < projects.size(); i++) {
                Project pro = projects.get(i);
                Row row = sheet.createRow(i + 1); // +1 because header is at row 0
                row.createCell(0).setCellValue(pro.getProject_id());
                row.createCell(1).setCellValue(pro.getProject_code());
                row.createCell(2).setCellValue(pro.getProject_en_name());
                row.createCell(3).setCellValue(pro.getProject_vi_name());
                row.createCell(4).setCellValue(pro.getProject_descript());
                row.createCell(5).setCellValue(pro.getStatus());
                row.createCell(6).setCellValue(pro.getClass_id());
                row.createCell(7).setCellValue(pro.getGroup_name());
                row.createCell(8).setCellValue(pro.getMentor_id());
                row.createCell(9).setCellValue(pro.getCo_mentor_id());
            }

            // Writing the workbook to the specified file
            try ( FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true; // Return true when file export is successful
        } catch (IOException e) {
            e.printStackTrace();  // Log the exception
            return false; // Return false when file export fails
        }
    }

    public boolean exportTemplateProject() {
        String filePath = "C:\\Users\\TRAN DUNG\\Downloads\\Template.xlsx";

        try ( Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Projects");

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("group_name");
            headerRow.createCell(1).setCellValue("project_code");
            headerRow.createCell(2).setCellValue("project_en_name");
            headerRow.createCell(3).setCellValue("project_vi_name");
            headerRow.createCell(4).setCellValue("project_descript");

            // Data rows
            Row row = sheet.createRow(2); // +1 because header is at row 0
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");

            // Writing the workbook to the specified file
            try ( FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true; // Return true when file export is successful
        } catch (IOException e) {
            e.printStackTrace();  // Log the exception
            return false; // Return false when file export fails
        }
    }

    public void importClassStudentFromExcel(String filePath) throws IOException, SQLException {
        ClassStudent classSt = new ClassStudent();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from 1 to skip header row
            Row row = sheet.getRow(i);
            int id = (int) row.getCell(0).getNumericCellValue();
            int classId = (int) row.getCell(1).getNumericCellValue();
            int studentId = (int) row.getCell(2).getNumericCellValue();
            int groupId = (int) row.getCell(3).getNumericCellValue();
            String groupName = row.getCell(4).getStringCellValue();
            String note = row.getCell(5).getStringCellValue();
            String studentName = row.getCell(6).getStringCellValue();
            classSt.setId(id);
            classSt.setClassId(classId);
            classSt.setStudentId(studentId);
            classSt.setGroupId(groupId);
            classSt.setGroupName(groupName);
            classSt.setNote(note);
            classSt.setStudentName(studentName);
            classSt.createNewClassSt();

        }

    }

    public void importProjectFromExcel(String filePath, int class_id) throws IOException, SQLException {
        Project pro = new Project();
        ProjectDAO dao = new ProjectDAO();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
        for (int i = 1; i < sheet.getLastRowNum(); i++) { // Start from 1 to skip header row
            Row row = sheet.getRow(i);
            String group_name = (String) row.getCell(0).getStringCellValue();
            String project_code = (String) row.getCell(1).getStringCellValue();
            String project_en_name = (String) row.getCell(2).getStringCellValue();
            String project_vi_name = (String) row.getCell(3).getStringCellValue();
            String project_descript = (String) row.getCell(4).getStringCellValue();

//            int mentor_id_INT = Integer.parseInt(mentor_id);
//            int co_mentor_id_INT = Integer.parseInt(co_mentor_id);
            pro.setProject_code(project_code);
            pro.setGroup_name(group_name);
            pro.setClass_id(class_id);
            pro.setProject_en_name(project_en_name);
            pro.setProject_vi_name(project_vi_name);
            pro.setProject_descript(project_descript);

            dao.importProExcel(pro);
        }

    }

    public static void main(String[] args) throws IOException, SQLException {
        ExcelHandler ex = new ExcelHandler();
        ex.exportTemplateProject();
    }
}
