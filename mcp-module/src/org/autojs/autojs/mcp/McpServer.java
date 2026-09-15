package org.autojs.autojs.mcp;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class McpServer extends NanoHTTPD {
    private final McpToolHandler toolHandler;
    
    public McpServer(int port) {
        super(port);
        this.toolHandler = new McpToolHandler();
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        if ("/mcp".equals(session.getUri()) && session.getMethod() == Method.POST) {
            try {
                int len = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] buf = new byte[len];
                session.getInputStream().read(buf);
                String body = new String(buf, "UTF-8");
                
                JSONObject request = new JSONObject(body);
                JSONObject response = handleRequest(request);
                
                return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("jsonrpc", "2.0");
                err.put("error", new JSONObject().put("code", -32603).put("message", e.getMessage()));
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", err.toString());
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", 
            new JSONObject().put("error", "Not Found").toString());
    }
    
    private JSONObject handleRequest(JSONObject req) {
        String method = req.optString("method", "");
        JSONObject resp = new JSONObject();
        resp.put("jsonrpc", "2.0");
        resp.put("id", req.opt("id"));
        
        switch (method) {
            case "initialize":
                resp.put("result", getServerInfo());
                break;
            case "tools/list":
                resp.put("result", getToolsList());
                break;
            case "tools/call":
                resp.put("result", toolHandler.callTool(req.optJSONObject("params")));
                break;
            default:
                resp.put("error", new JSONObject().put("code", -32601).put("message", "Method not found: " + method));
        }
        return resp;
    }
    
    private JSONObject getServerInfo() {
        JSONObject info = new JSONObject();
        info.put("protocolVersion", "2024-11-05");
        info.put("serverName", "AutoJs6 MCP Server");
        info.put("serverVersion", "1.0.0");
        JSONObject caps = new JSONObject();
        caps.put("tools", new JSONObject());
        info.put("capabilities", caps);
        return info;
    }
    
    private JSONObject getToolsList() {
        JSONObject result = new JSONObject();
        JSONArray tools = new JSONArray();
        
        String[][] toolDefs = {
            {"runScript", "执行Auto.js脚本", "script", "要执行的脚本代码"},
            {"captureScreen", "截取当前屏幕", "format", "截图格式(png/jpeg)"},
            {"getUiLayout", "获取当前UI布局", "depth", "遍历深度(默认3)"},
            {"checkSyntax", "检查脚本语法", "script", "要检查的脚本代码"},
            {"stopAllScripts", "停止所有运行中的脚本", "", ""},
            {"getMcpLog", "获取MCP运行日志", "lines", "返回日志行数(默认50)"},
            {"getServerInfo", "获取服务器信息", "", ""}
        };
        
        for (String[] def : toolDefs) {
            JSONObject tool = new JSONObject();
            tool.put("name", def[0]);
            tool.put("description", def[1]);
            JSONObject schema = new JSONObject();
            schema.put("type", "object");
            JSONObject props = new JSONObject();
            if (!def[2].isEmpty()) {
                JSONObject prop = new JSONObject();
                prop.put("type", "string");
                prop.put("description", def[3]);
                props.put(def[2], prop);
            }
            schema.put("properties", props);
            tool.put("inputSchema", schema);
            tools.put(tool);
        }
        
        result.put("tools", tools);
        return result;
    }
}
