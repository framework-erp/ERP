package erp.process.definition.json;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessJsonUtil {

    private static Pattern createdEntityListPattern = Pattern.compile("\\{.*?\"name\".*?\"argumentList\".*?\"result\".*?\"createdEntityList\":\\[(.*?)],\"deletedEntityList\":");
    private static Pattern deletedEntityListPattern = Pattern.compile("\\{.*?\"name\".*?\"argumentList\".*?\"result\".*?\"deletedEntityList\":\\[(.*?)],\"entityUpdateList\":");
    private static Pattern entityUpdateListPattern = Pattern.compile("\\{.*?\"name\".*?\"argumentList\".*?\"result\".*?\"entityUpdateList\":\\[(.*?)]\\}$");
    private static Pattern typedEntityPattern = Pattern.compile("\\{\"type\":\"(.*?)\",\"entity\":(.*?)\\}$");
    private static Pattern typedEntityUpdatePattern = Pattern.compile("\\{\"type\":\"(.*?)\",\"originalEntity\":(\\{.*?\\}),\"updatedEntity\":(.*?)\\}$");

    public static List<TypedEntityJson> getCreatedEntityListJson(String processJson) {
        Matcher createdEntityListMatcher = createdEntityListPattern.matcher(processJson);
        createdEntityListMatcher.find();
        String createdEntityListJson = createdEntityListMatcher.group(1);
        return parseEntityListJson(createdEntityListJson);
    }

    public static List<TypedEntityJson> getDeletedEntityListJson(String processJson) {
        Matcher deletedEntityListMatcher = deletedEntityListPattern.matcher(processJson);
        deletedEntityListMatcher.find();
        String deletedEntityListJson = deletedEntityListMatcher.group(1);
        return parseEntityListJson(deletedEntityListJson);
    }

    public static List<TypedEntityUpdateJson> getEntityUpdateListJson(String processJson) {
        Matcher entityUpdateListMatcher = entityUpdateListPattern.matcher(processJson);
        entityUpdateListMatcher.find();
        String entityUpdateListJson = entityUpdateListMatcher.group(1);
        return parseEntityUpdateListJson(entityUpdateListJson);
    }

    private static List<TypedEntityUpdateJson> parseEntityUpdateListJson(String entityListJson) {
        List<TypedEntityUpdateJson> list = new ArrayList<>();
        int iStart = -1;
        int count = 0;
        for (int i = 0; i < entityListJson.length(); i++) {
            if (entityListJson.charAt(i) == '{') {
                if (iStart == -1) {
                    iStart = i;
                }
                count++;
            }
            if (entityListJson.charAt(i) == '}') {
                count--;
                if (count == 0) {
                    String typedEntityUpdateJson = entityListJson.substring(iStart, i + 1);
                    Matcher matcher = typedEntityUpdatePattern.matcher(typedEntityUpdateJson);
                    matcher.find();
                    TypedEntityUpdateJson typedEntityUpdateJsonObj = new TypedEntityUpdateJson();
                    typedEntityUpdateJsonObj.setType(matcher.group(1));
                    typedEntityUpdateJsonObj.setOriginalEntityJson(matcher.group(2));
                    typedEntityUpdateJsonObj.setUpdatedEntityJson(matcher.group(3));
                    list.add(typedEntityUpdateJsonObj);
                    iStart = -1;
                }
            }
        }
        return list;
    }

    private static List<TypedEntityJson> parseEntityListJson(String entityListJson) {
        List<TypedEntityJson> list = new ArrayList<>();
        int iStart = -1;
        int count = 0;
        for (int i = 0; i < entityListJson.length(); i++) {
            if (entityListJson.charAt(i) == '{') {
                if (iStart == -1) {
                    iStart = i;
                }
                count++;
            }
            if (entityListJson.charAt(i) == '}') {
                count--;
                if (count == 0) {
                    String typedEntityJson = entityListJson.substring(iStart, i + 1);
                    Matcher matcher = typedEntityPattern.matcher(typedEntityJson);
                    matcher.find();
                    TypedEntityJson typedEntityJsonObj = new TypedEntityJson();
                    typedEntityJsonObj.setType(matcher.group(1));
                    typedEntityJsonObj.setEntityJson(matcher.group(2));
                    list.add(typedEntityJsonObj);
                    iStart = -1;
                }
            }
        }
        return list;
    }
}
