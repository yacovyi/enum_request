import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Statistics {

    public static final String STRICT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    private String httpMethod;
    private String enumListName;
    private String language;
    private ZonedDateTime startRequestTime;
    private ZonedDateTime endRequestTime;
    private long requestTimeMS;
    private String threadName;

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getEnumListName() {
        return enumListName;
    }

    public void setEnumListName(String enumListName) {
        this.enumListName = enumListName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStartRequestTime() {
        return getInStrictDateTimeFormat(startRequestTime);
    }

    public void setStartRequestTime(ZonedDateTime startRequestTime) {
        this.startRequestTime = startRequestTime;
    }

    public String getEndRequestTime() {
        return getInStrictDateTimeFormat(endRequestTime);
    }

    public void setEndRequestTime(ZonedDateTime endRequestTime) {
        this.endRequestTime = endRequestTime;
        this.requestTimeMS = ChronoUnit.MILLIS.between(startRequestTime, endRequestTime);
    }

    public long getRequestTimeMS() {
        return requestTimeMS;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getInStrictDateTimeFormat(ZonedDateTime dateTime) {

        if(dateTime == null)
            return null;

        return dateTime.format(DateTimeFormatter.ofPattern(STRICT_DATE_TIME_FORMAT));
    }
}
