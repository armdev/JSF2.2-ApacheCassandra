package com.project.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "userRegister")
@ViewScoped
public class UserRegister implements Serializable {

    private static final long serialVersionUID = 1L;
    @ManagedProperty("#{databaseBean}")
    private DatabaseBean databaseBean;
    private List<User> resultList = new ArrayList<User>();
    private User user;

    public UserRegister() {

    }

    @PostConstruct
    public void init() {
        user = new User();
    }

    public void doClear() {
        resultList.clear();
    }

    public void doRegister() {
        databaseBean.createPerson(user);

    }

    public List<User> getResultList() {
        resultList = databaseBean.getUserListSorted();
        return resultList;
    }

    public void setResultList(List<User> resultList) {
        this.resultList = resultList;
    }

    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
