package fanap.dinner.domain.model.group;

//This groups are reserved for Fanap Dinner project.
public enum DinnerGroup {

    /*
    All groups must start with this
     */
    FANAP_DINNER_,

    /*
    All users who are logged in to the project are also members of this group.
     */
    FANAP_DINNER_USER,
    /*
    This group is for admins. They have access to most services.
     */
    FANAP_DINNER_ADMIN,
    /*
    This group is for admins that just have access to reports.
    */
    FANAP_DINNER_ADMIN_REPORT

}
