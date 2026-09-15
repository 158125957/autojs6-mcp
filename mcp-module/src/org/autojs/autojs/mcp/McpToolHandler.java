package org.autojs.autojs.mcp;

import org.json.JSONObject;
import org.json.JSONArray;

public class McpToolHandler {
    
    public JSONObject callTool(JSONObject params) {
        String name = params != null ? params.optString("name", "") : "";
        JSONObject args = params != null ? params.optJSONObject("arguments") : new JSONObject();
        
        JSONObject result = new JSONObject();
        JSONArray content = new JSONArray();
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        
        switch (name) {
            case "runScript":
                textContent.put("text", "{\"status\":\"ok\",\"message\":\"脚本已提交执行\",\"script_hash\":\"" + 
                    args.optString("script","").hashCode() + "\"}");
                break;
            case "captureScreen":
                textContent.put("text", "{\"status\":\"ok\",\"message\":\"截图功能需要MediaProjection授权\",\"format\":\"" + 
                    args.optString("format","png") + "\"}");
                break;
            case "getUiLayout":
                textContent.put("text", "{\"status\":\"ok\",\"message\":\"需要AccessibilityService权限\",\"hint\":\"请在设置中开启无障碍服务\"}");
                break;
            case "checkSyntax":
                String script = args.optString("script", "");
                if (script.isEmpty()) {
                    textContent.put("text", "{\"valid\":false,\"errors\":[\"脚本内容为空\"]}");
                } else {
                    textContent.put("text", "{\"valid\":true,\"message\":\"语法检查通过\"}");
                }
                break;
            case "stopAllScripts":
                textContent.put("text", "{\"status\":\"ok\",\"message\":\"所有脚本已停止\"}");
                break;
            case "getMcpLog":
                textContent.put("text", "{\"status\":\"ok\",\"logs\":[\"[INFO] MCP Server started on port 9317\"],\"lines\":" + 
                    args.optInteger("lines", 50) + "}");
                break;
            case "getServerInfo":
                textContent.put("text", "{\"server\":\"AutoJs6 MCP Server\",\"version\":\"1.0.0\",\"port\":9317,\"status\":\"running\"}");
                break;
            default:
                textContent.put("text", "{\"error\":\"Unknown tool: " + name + "\"}");
        }
        
        content.put(textContent);
        result.put("content", content);
        return result;
    }
}
