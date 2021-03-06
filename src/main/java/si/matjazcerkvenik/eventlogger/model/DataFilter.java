package si.matjazcerkvenik.eventlogger.model;

public class DataFilter {

    private String sort;
    private int limit;
    private String[] hosts;
    private String[] idents;
    private String regex;
    private long fromDate;
    private long toDate;


    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public String[] getIdents() {
        return idents;
    }

    public void setIdents(String[] idents) {
        this.idents = idents;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "DataFilter{" +
                "sort='" + sort + '\'' +
                ", limit=" + limit +
                ", hosts=" + hosts +
                ", ident=" + idents +
                ", regex='" + regex + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
