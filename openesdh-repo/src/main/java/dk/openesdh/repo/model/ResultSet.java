package dk.openesdh.repo.model;

import java.util.List;

public class ResultSet<T> {

    private List<T> resultList;

    private int totalItems;

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

}
