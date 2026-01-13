package com.example.ju_os.db;

import com.example.ju_os.config.QueryConfig.ParamDefinition;

import java.util.*;
import java.util.regex.*;

public class QueryParser {
    
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile(
        "\{%\s*if\s+(\w+)\s*%\}\n?(.*?)\n?\{%\s*endif\s*%\}",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PARAM_PATTERN = Pattern.compile(":(\w+)");

    public static ParsedQuery parse(QueryConfig.QueryDefinition queryDef, Map<String, Object> inputParams) {
        String sql = queryDef.sql;
        List<ParamDefinition> paramDefs = queryDef.params != null ? queryDef.params : new ArrayList<>();
        
        // Build parameter map with defaults
        Map<String, Object> paramValues = new LinkedHashMap<>();
        Map<String, ParamDefinition> paramMeta = new LinkedHashMap<>();
        
        for (ParamDefinition pd : paramDefs) {
            paramMeta.put(pd.name, pd);
            Object value = inputParams.get(pd.name);
            if (value == null && pd.defaultValue != null) {
                value = pd.defaultValue;
            }
            paramValues.put(pd.name, value);
        }
        
        // Validate required parameters
        List<String> missingRequired = new ArrayList<>();
        for (ParamDefinition pd : paramDefs) {
            if (pd.required && paramValues.get(pd.name) == null) {
                missingRequired.add(pd.name);
            }
        }
        if (!missingRequired.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameters: " + String.join(", ", missingRequired));
        }
        
        // Process conditionals
        sql = processConditionals(sql, paramValues);
        
        // Extract parameters from SQL
        Set<String> sqlParams = extractParams(sql);
        
        // Build final parameter list in SQL order
        List<NamedParam> orderedParams = new ArrayList<>();
        for (String paramName : sqlParams) {
            Object value = paramValues.get(paramName);
            ParamDefinition meta = paramMeta.get(paramName);
            if (value != null) {
                orderedParams.add(new NamedParam(paramName, value, meta != null ? meta.type : "string"));
            }
        }
        
        return new ParsedQuery(sql, orderedParams);
    }

    private static String processConditionals(String sql, Map<String, Object> paramValues) {
        Matcher matcher = CONDITIONAL_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            String fragment = matcher.group(2);
            Object value = paramValues.get(paramName);
            
            // Include fragment only if parameter is present and truthy
            if (value != null && !value.toString().isEmpty() && !"null".equalsIgnoreCase(value.toString())) {
                matcher.appendReplacement(result, fragment.trim());
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private static Set<String> extractParams(String sql) {
        Set<String> params = new LinkedHashSet<>();
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }

    public static class ParsedQuery {
        public final String sql;
        public final List<NamedParam> params;

        public ParsedQuery(String sql, List<NamedParam> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    public static class NamedParam {
        public final String name;
        public final Object value;
        public final String type;

        public NamedParam(String name, Object value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }
    }
}
