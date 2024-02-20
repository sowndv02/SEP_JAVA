/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import dal.MilestoneDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gitlab4j.api.Pager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Milestone; 
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectGroup;
import org.gitlab4j.api.models.ProjectUser;
import org.gitlab4j.api.models.User;

/**
 *
 * @author Đàm Quang Chiến
 */
public class SyncGitLab {

    private String projectId, accessToken, groupId;

    public SyncGitLab() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void syncMilestones(List<model.Milestone> listClassMilestoneDB, int classId) throws GitLabApiException {
        List<org.gitlab4j.api.models.Milestone> listClassMilestoneGit = this.getListGroupMilestones();
        Map<String, model.Milestone> dbMilestonesMap = new HashMap<>();
        for (model.Milestone dbMilestone : listClassMilestoneDB) {
            dbMilestonesMap.put(dbMilestone.getGitlabId(), dbMilestone);
        }

        Map<String, org.gitlab4j.api.models.Milestone> gitlabMilestonesMap = new HashMap<>();
        for (org.gitlab4j.api.models.Milestone gitlabMilestone : listClassMilestoneGit) {
            gitlabMilestonesMap.put(String.valueOf(gitlabMilestone.getId()), gitlabMilestone);
        }
        for (org.gitlab4j.api.models.Milestone gitlabMilestone : listClassMilestoneGit) {
            String milestoneId = String.valueOf(gitlabMilestone.getId());
            // Check if GitLab milestone exists in the database list based on title
            if (dbMilestonesMap.containsKey(milestoneId)) {
                model.Milestone dbMilestone = dbMilestonesMap.get(milestoneId);

                // Compare "updatedAt" to determine which one is more recent
                Date gitlabUpdatedAt = gitlabMilestone.getUpdatedAt();

                // Parse the GitLab "updatedAt" time as a java.util.Date object
                Date dbUpdatedAt = dbMilestone.getUpdateAt();

                if (gitlabUpdatedAt != null && dbUpdatedAt != null && gitlabUpdatedAt.compareTo(dbUpdatedAt) > 0) {
                    // Update the database milestone with GitLab data
                    updateDatabaseMilestone(dbMilestone, gitlabMilestone);
                }
            } else {
                // Milestone doesn't exist in the database, so add it
                addMilestoneToDatabase(gitlabMilestone, classId);
            }
        }

        for (model.Milestone dbMilestone : listClassMilestoneDB) {
            String dbGitMileId = dbMilestone.getGitlabId();
            if(dbMilestone.getIsActive() != 2){
                if (gitlabMilestonesMap.containsKey(dbGitMileId)) {
                    org.gitlab4j.api.models.Milestone gitlabMilestone = gitlabMilestonesMap.get(dbGitMileId);

                    // Compare "updatedAt" to determine which one is more recent
                    Date gitlabUpdatedAt = gitlabMilestone.getUpdatedAt();

                    // Parse the GitLab "updatedAt" time as a java.util.Date object
                    Date dbUpdatedAt = dbMilestone.getUpdateAt();

                    if (gitlabUpdatedAt != null && dbUpdatedAt != null && gitlabUpdatedAt.compareTo(dbUpdatedAt) < 0) {
                        // Update the database milestone with GitLab data
                        updateGitLabMilestone(dbMilestone, gitlabMilestone);
                    }
                } else {
                    // Milestone doesn't exist in the database, so add it
                    createGitLabMilestone(dbMilestone);
                }
            }
            // Check if GitLab milestone exists in the database list based on title
        }
    }

    public void syncClassSetting(List<model.Milestone> listClassMilestoneDB, int classId) throws GitLabApiException {

    }

    private void updateDatabaseMilestone(model.Milestone dbMilestone, org.gitlab4j.api.models.Milestone gitlabMilestone) {
        System.out.println("update db milestone");
        dbMilestone.setName(gitlabMilestone.getTitle());
        dbMilestone.setDescript(gitlabMilestone.getDescription());
        dbMilestone.setStartDate(gitlabMilestone.getStartDate());
        dbMilestone.setDueDate(gitlabMilestone.getDueDate());
        dbMilestone.setIsActive(gitlabMilestone.getState().equals("active") ? 1 : 0);
        boolean result = dbMilestone.updateMilestone();
    }

    private void updateGitLabMilestone(model.Milestone dbMilestone, org.gitlab4j.api.models.Milestone gitlabMilestone) throws GitLabApiException {
        System.out.println("update gitlab milesotne");
        this.updateClassMilestone(gitlabMilestone.getId(), dbMilestone.getName(), dbMilestone.getDescript(), dbMilestone.getDueDate(), dbMilestone.getStartDate(), dbMilestone.getIsActive());
        System.out.println("Gitlab Updated ");
    }

//    private void updateDatabaseSetting(model.Class classSetitng, org.gitlab4j.api.models.Group group) {
//
//    }
//
//    private void updateGitLabSetting(model.Class classSetitng, org.gitlab4j.api.models.Group group) throws GitLabApiException {
//        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com", this.accessToken);
//        GroupParams params = new GroupParams();
//        params.withDescription(classSetitng.getClassDetails());
//        String subUrl = classSetitng.getGitlabId().substring("https://gitlab.com/".length());
//        params.withPath(subUrl);
//        gitLabApi.getGroupApi().updateGroup("fuhn/fall23/swp391-demo/se1741-check", params);
//        System.out.println("Gitlab Updated ");
//    }

    private void addMilestoneToDatabase(org.gitlab4j.api.models.Milestone gitlabMilestone, int classId) {
        model.Milestone milestone = new model.Milestone();

        milestone.setName(gitlabMilestone.getTitle());
        milestone.setDescript(gitlabMilestone.getDescription());
        milestone.setStartDate(gitlabMilestone.getStartDate());
        milestone.setDueDate(gitlabMilestone.getDueDate());
        milestone.setClassId(classId);
        milestone.setIsActive(gitlabMilestone.getState().equals("active") ? 1 : 0);
        milestone.setGitlabId(String.valueOf(gitlabMilestone.getId()));
        boolean result = milestone.insertMilestone();
    }

    private void createGitLabMilestone(model.Milestone dbMilestone) throws GitLabApiException {
        this.createClassMilestone(dbMilestone.getName(), dbMilestone.getDescript(), dbMilestone.getDueDate(), dbMilestone.getStartDate(), dbMilestone.getIsActive());
    }

    public List<Milestone> getListGroupMilestones() throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com", this.accessToken);
        return gitLabApi.getMilestonesApi().getGroupMilestones(this.groupId);
    }

    public void updateClassMilestone(long milestoneId, String title, String des, Date dueDate, Date startDate, int status) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com", this.accessToken);
        gitLabApi.getMilestonesApi().updateGroupMilestone(groupId, milestoneId, title, des, dueDate, startDate, status == 1 ? Constants.MilestoneState.ACTIVATE : Constants.MilestoneState.CLOSE);
    }

    public void createClassMilestone(String title, String des, Date dueDate, Date startDate, int status) throws GitLabApiException {
        MilestoneDAO mileDAO = new MilestoneDAO();
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com", this.accessToken);
        gitLabApi.getMilestonesApi().createGroupMilestone(groupId, title, des, dueDate, startDate);

        List<Milestone> list = this.getListGroupMilestones();
        Milestone milestone = null;
        for (Milestone mile : list) {
            if (mile.getTitle().equals(title)) {
                milestone = mile;
                break;
            }
        }
        long milestoneId = milestone.getId();
        mileDAO.updateGitlabMilestone(title, String.valueOf(milestoneId));
        this.updateClassMilestone(milestoneId, title, des, dueDate, startDate, status);
    }

    public static void main(String[] args) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com", "glpat-eZKiCLfVzJk3akdkaro_");
        Group group = gitLabApi.getGroupApi().getGroup("fuhn/fall23/swp391-demo/se1741-check");
        System.out.println(group);
    }

}
