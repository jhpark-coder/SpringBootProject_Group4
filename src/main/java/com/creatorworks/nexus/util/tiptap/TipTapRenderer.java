package com.creatorworks.nexus.util.tiptap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * TipTapNode 객체 리스트를 HTML 문자열로 렌더링하는 클래스입니다.
 */
@Component
public class TipTapRenderer {

    /**
     * 최상위 노드 리스트를 HTML로 렌더링합니다.
     * @param nodes 렌더링할 TipTapNode 리스트
     * @return 생성된 HTML 문자열
     */
    public String render(List<TipTapNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }
        return nodes.stream()
                .map(this::renderNode)
                .collect(Collectors.joining());
    }

    /**
     * 단일 노드를 재귀적으로 렌더링합니다.
     * @param node 렌더링할 TipTapNode
     * @return 생성된 HTML 문자열
     */
    private String renderNode(TipTapNode node) {
        if (node == null) return "";

        // 1. 자식 노드들을 먼저 렌더링하여 내부 콘텐츠(innerContent)를 만듭니다.
        String innerContent = (node.getContent() != null) ? render(node.getContent()) : "";

        // 2. 텍스트 노드인 경우, 마크(스타일)를 적용합니다.
        if ("text".equals(node.getType())) {
            String text = HtmlUtils.htmlEscape(node.getText() != null ? node.getText() : "");
            if (node.getMarks() != null && !node.getMarks().isEmpty()) {
                StringBuilder styledText = new StringBuilder(text);
                for (TipTapMark mark : node.getMarks()) {
                    switch (mark.getType()) {
                        case "bold":
                            styledText.insert(0, "<strong>").append("</strong>");
                            break;
                        case "italic":
                            styledText.insert(0, "<em>").append("</em>");
                            break;
                        case "underline":
                            styledText.insert(0, "<u>").append("</u>");
                            break;
                        case "link":
                            Map<String, Object> linkAttrs = mark.getAttrs();
                            if (linkAttrs != null && linkAttrs.containsKey("href")) {
                                String href = linkAttrs.get("href").toString();
                                String target = linkAttrs.getOrDefault("target", "_blank").toString();
                                styledText.insert(0, "<a href=\"" + HtmlUtils.htmlEscape(href) + "\" target=\"" + HtmlUtils.htmlEscape(target) + "\">").append("</a>");
                            }
                            break;
                        case "textStyle":
                            Map<String, Object> styleAttrs = mark.getAttrs();
                            if (styleAttrs != null && !styleAttrs.isEmpty()) {
                                StringBuilder style = new StringBuilder();
                                if (styleAttrs.get("fontSize") != null) {
                                    style.append("font-size: ").append(styleAttrs.get("fontSize")).append(";");
                                }
                                if (styleAttrs.get("color") != null) {
                                    style.append("color: ").append(styleAttrs.get("color")).append(";");
                                }
                                if (styleAttrs.get("backgroundColor") != null) {
                                    style.append("background-color: ").append(styleAttrs.get("backgroundColor")).append(";");
                                }
                                if (style.length() > 0) {
                                    styledText.insert(0, "<span style=\"" + HtmlUtils.htmlEscape(style.toString()) + "\">").append("</span>");
                                }
                            }
                            break;
                    }
                }
                return styledText.toString();
            }
            return text;
        }

        // 3. 블록 노드인 경우, 타입에 맞는 HTML 태그로 감싸줍니다.
        switch (node.getType()) {
            case "paragraph":
                String paraStyle = buildStyleString(node.getAttrs());
                return "<p" + paraStyle + ">" + (innerContent.isEmpty() ? "&nbsp;" : innerContent) + "</p>";
            case "heading":
                int level = 2; // 기본 레벨 h2
                Map<String, Object> attrs = node.getAttrs();
                if (attrs != null) {
                    Object levelAttr = attrs.get("level");
                    if (levelAttr instanceof Integer) {
                        level = (Integer) levelAttr;
                    } else if (levelAttr instanceof Double) {
                        level = ((Double) levelAttr).intValue();
                    } else if (levelAttr instanceof String) {
                        try {
                            level = Integer.parseInt((String) levelAttr);
                        } catch (NumberFormatException e) {
                            level = 2;
                        }
                    }
                    if (level < 1 || level > 6) level = 2;
                }
                String headingStyle = buildStyleString(attrs);
                return "<h" + level + headingStyle + ">" + innerContent + "</h" + level + ">";
            case "image":
                Map<String, Object> imageAttrs = node.getAttrs();
                if (imageAttrs != null && imageAttrs.get("src") != null) {
                    String src = imageAttrs.get("src").toString();
                    String alt = imageAttrs.getOrDefault("alt", "").toString();
                    String caption = imageAttrs.get("caption") != null ? imageAttrs.get("caption").toString() : null;
                    
                    StringBuilder style = new StringBuilder();
                    if (imageAttrs.get("width") != null) {
                        style.append("width: ").append(HtmlUtils.htmlEscape(imageAttrs.get("width").toString())).append(";");
                    }
                    if (imageAttrs.get("data-float") != null) {
                        style.append("float: ").append(HtmlUtils.htmlEscape(imageAttrs.get("data-float").toString())).append(";");
                    }

                    String imgTag = "<img src=\"" + HtmlUtils.htmlEscape(src) + "\" alt=\"" + HtmlUtils.htmlEscape(alt) + "\" style=\"" + style.toString() + "\">";
                    String textAlign = imageAttrs.get("textAlign") != null ? imageAttrs.get("textAlign").toString() : null;

                    if (caption != null && !caption.isEmpty()) {
                        String figureStyle = (textAlign != null) ? "style=\"text-align:" + textAlign + ";\"" : "";
                        return "<figure " + figureStyle + ">" + imgTag + "<figcaption>" + HtmlUtils.htmlEscape(caption) + "</figcaption></figure>";
                    }

                    if (textAlign != null) {
                        return "<div class=\"image-wrapper has-text-align-" + textAlign + "\" style=\"text-align:" + textAlign + ";\">" + imgTag + "</div>";
                    }
                    
                    return imgTag;
                }
                return "";
            case "codeBlockNode":
                Map<String, Object> codeBlockAttrs = node.getAttrs();
                String language = codeBlockAttrs != null ? (String) codeBlockAttrs.getOrDefault("language", "auto") : "auto";
                String caption = codeBlockAttrs != null ? (String) codeBlockAttrs.getOrDefault("caption", "") : "";
                
                StringBuilder codeBlockHtml = new StringBuilder();
                codeBlockHtml.append("<div class=\"custom-code-block-wrapper\">");
                codeBlockHtml.append("<div class=\"code-block-container\">");
                codeBlockHtml.append("<div class=\"code-block-header\">");
                codeBlockHtml.append("<select class=\"language-selector\" contenteditable=\"false\">");
                codeBlockHtml.append("<option value=\"auto\"").append("auto".equals(language) ? " selected" : "").append(">auto</option>");
                codeBlockHtml.append("<option value=\"java\"").append("java".equals(language) ? " selected" : "").append(">Java</option>");
                codeBlockHtml.append("<option value=\"python\"").append("python".equals(language) ? " selected" : "").append(">Python</option>");
                codeBlockHtml.append("<option value=\"javascript\"").append("javascript".equals(language) ? " selected" : "").append(">JavaScript</option>");
                codeBlockHtml.append("<option value=\"typescript\"").append("typescript".equals(language) ? " selected" : "").append(">TypeScript</option>");
                codeBlockHtml.append("<option value=\"html\"").append("html".equals(language) ? " selected" : "").append(">HTML</option>");
                codeBlockHtml.append("<option value=\"css\"").append("css".equals(language) ? " selected" : "").append(">CSS</option>");
                codeBlockHtml.append("</select>");
                codeBlockHtml.append("<div class=\"code-block-toolbar\">");
                codeBlockHtml.append("<button title=\"Copy code\"><span>Copy</span></button>");
                codeBlockHtml.append("<button title=\"Add caption\"><span>Caption</span></button>");
                codeBlockHtml.append("<button title=\"Delete node\"></button>");
                codeBlockHtml.append("</div>");
                codeBlockHtml.append("</div>");
                codeBlockHtml.append("<pre class=\"code-block-content\" spellcheck=\"false\">");
                codeBlockHtml.append("<code>").append(innerContent).append("</code>");
                codeBlockHtml.append("</pre>");
                codeBlockHtml.append("</div>");
                codeBlockHtml.append("</div>");
                
                return codeBlockHtml.toString();
            case "audio":
                Map<String, Object> audioAttrs = node.getAttrs();
                if (audioAttrs != null && audioAttrs.get("src") != null) {
                    String src = audioAttrs.get("src").toString();
                    String width = audioAttrs.getOrDefault("width", "100%").toString();
                    String textAlign = audioAttrs.get("textAlign") != null ? audioAttrs.get("textAlign").toString() : null;

                    String audioTag = "<audio src=\"" + HtmlUtils.htmlEscape(src) + "\" controls=\"true\" style=\"width: " + HtmlUtils.htmlEscape(width) + ";\"></audio>";
                    
                    String wrapperStyle = (textAlign != null) ? "style=\"text-align:" + textAlign + ";\"" : "";
                    return "<div class=\"audio-wrapper\" " + wrapperStyle + ">" + audioTag + "</div>";
                }
                return "";
            case "video":
                Map<String, Object> videoAttrs = node.getAttrs();
                if (videoAttrs != null && videoAttrs.get("src") != null) {
                    String src = videoAttrs.get("src").toString();
                    String width = videoAttrs.getOrDefault("width", "100%").toString();
                    String textAlign = videoAttrs.getOrDefault("textAlign", "center").toString();

                    StringBuilder wrapperStyle = new StringBuilder("width: " + HtmlUtils.htmlEscape(width) + ";");
                     if ("center".equals(textAlign)) {
                        wrapperStyle.append("margin-left: auto; margin-right: auto;");
                    } else if ("right".equals(textAlign)) {
                        wrapperStyle.append("margin-left: auto; margin-right: 0;");
                    } else {
                        wrapperStyle.append("margin-left: 0; margin-right: auto;");
                    }

                    String videoTag = "<video src=\"" + HtmlUtils.htmlEscape(src) + "\" controls=\"true\"></video>";
                    
                    return "<div class=\"video-wrapper\" style=\"" + wrapperStyle.toString() + "\">" + videoTag + "</div>";
                }
                return "";
            case "iframe":
                Map<String, Object> iframeAttrs = node.getAttrs();
                if (iframeAttrs != null && iframeAttrs.get("src") != null) {
                    String src = iframeAttrs.get("src").toString();
                    String width = iframeAttrs.getOrDefault("width", "100%").toString();
                    String textAlign = iframeAttrs.getOrDefault("textAlign", "center").toString();

                    StringBuilder wrapperStyle = new StringBuilder("width: " + HtmlUtils.htmlEscape(width) + ";");
                    if ("center".equals(textAlign)) {
                        wrapperStyle.append("margin-left: auto; margin-right: auto;");
                    } else if ("right".equals(textAlign)) {
                        wrapperStyle.append("margin-left: auto; margin-right: 0;");
                    } else {
                        wrapperStyle.append("margin-left: 0; margin-right: auto;");
                    }

                    String iframeTag = "<iframe src=\"" + HtmlUtils.htmlEscape(src) + "\" "
                        + "frameborder=\"0\" "
                        + "allowfullscreen=\"true\" "
                        + "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" "
                        + "referrerpolicy=\"strict-origin-when-cross-origin\">"
                        + "</iframe>";
                    
                    return "<div class=\"iframe-wrapper\" style=\"" + wrapperStyle.toString() + "\">" + iframeTag + "</div>";
                }
                return "";
            case "horizontalRule":
                return "<hr>";
            case "bulletList":
                return "<ul>" + innerContent + "</ul>";
            case "orderedList":
                return "<ol>" + innerContent + "</ol>";
            case "listItem":
                return "<li>" + innerContent + "</li>";
            case "hardBreak":
                return "<br>";
            case "paywall":
                return ""; // 페이월 노드는 의도적으로 아무것도 렌더링하지 않습니다.
            case "spacerNode":
                Map<String, Object> spacerAttrs = node.getAttrs();
                if (spacerAttrs != null && spacerAttrs.get("height") != null) {
                    String height = spacerAttrs.get("height").toString();
                    return "<div data-type=\"spacer\" style=\"height: " + HtmlUtils.htmlEscape(height) + ";\"></div>";
                }
                return "<div data-type=\"spacer\" style=\"height: 2rem;\"></div>"; // 기본값

            case "photoGrid":
                Map<String, Object> gridAttrs = node.getAttrs();
                if (gridAttrs != null && gridAttrs.get("items") instanceof List) {
                    List<Map<String, String>> items = (List<Map<String, String>>) gridAttrs.get("items");
                    String layout = gridAttrs.getOrDefault("layout", "2-cols").toString();
                    Map<String, Object> savedLayouts = (Map<String, Object>) gridAttrs.getOrDefault("savedLayouts", new HashMap<>());
                    
                    String layoutClass = layout.startsWith("grid-") ? layout : "grid-" + layout;

                    StringBuilder itemsHtml = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        final int index = i;
                        Map<String, String> item = items.get(i);
                        String src = item.get("src");
                        if (src == null || src.isEmpty()) {
                            continue;
                        }
                        String alt = item.getOrDefault("alt", "");
                        
                        // 저장된 레이아웃 정보에서 해당 아이템의 위치/크기 가져오기
                        StringBuilder itemStyle = new StringBuilder();
                        if (savedLayouts.containsKey("lg")) {
                            List<Map<String, Object>> lgLayout = (List<Map<String, Object>>) savedLayouts.get("lg");
                            Map<String, Object> itemLayout = lgLayout.stream()
                                .filter(l -> l.get("i").equals(String.valueOf(index)))
                                .findFirst()
                                .orElse(null);
                            
                            if (itemLayout != null) {
                                int x = ((Number) itemLayout.get("x")).intValue();
                                int y = ((Number) itemLayout.get("y")).intValue();
                                int w = ((Number) itemLayout.get("w")).intValue();
                                int h = ((Number) itemLayout.get("h")).intValue();
                                itemStyle.append("grid-column: ").append(x + 1).append(" / span ").append(w).append(";");
                                itemStyle.append("grid-row: ").append(y + 1).append(" / span ").append(h).append(";");
                            }
                        }
                        
                        itemsHtml.append("<div class=\"grid-item\"");
                        if (itemStyle.length() > 0) {
                            itemsHtml.append(" style=\"").append(HtmlUtils.htmlEscape(itemStyle.toString())).append("\"");
                        }
                        itemsHtml.append("><img src=\"")
                                 .append(HtmlUtils.htmlEscape(src))
                                 .append("\" alt=\"").append(HtmlUtils.htmlEscape(alt)).append("\"");
                        
                        // 기존 width/height 처리 (호환성을 위해 유지)
                        String width = item.getOrDefault("width", "");
                        String height = item.getOrDefault("height", "");
                        StringBuilder style = new StringBuilder();
                        if (!width.isEmpty()) {
                            if (width.matches(".*[a-zA-Z%]$")) {
                                style.append("width:").append(HtmlUtils.htmlEscape(width)).append(";");
                            } else {
                                style.append("width:").append(HtmlUtils.htmlEscape(width)).append("px;");
                            }
                        }
                        if (!height.isEmpty()) {
                            if (height.matches(".*[a-zA-Z%]$")) {
                                style.append("height:").append(HtmlUtils.htmlEscape(height)).append(";");
                            } else {
                                style.append("height:").append(HtmlUtils.htmlEscape(height)).append("px;");
                            }
                        }
                        if (style.length() > 0) {
                            itemsHtml.append(" style=\"").append(style).append("\"");
                        }
                        itemsHtml.append("></div>");
                    }
                    
                    // 저장된 레이아웃 정보를 data-layouts 속성으로 추가
                    StringBuilder wrapperAttrs = new StringBuilder();
                    wrapperAttrs.append("class=\"photo-grid-wrapper ").append(HtmlUtils.htmlEscape(layoutClass)).append("\"");
                    if (!savedLayouts.isEmpty()) {
                        wrapperAttrs.append(" data-layouts=\"").append(HtmlUtils.htmlEscape(savedLayouts.toString())).append("\"");
                    }
                    
                    // wrapper 는 CSS Grid 로 표시. 별도 width/height 필요없음
                    return "<div " + wrapperAttrs.toString() + ">" + itemsHtml.toString() + "</div>";
                }
                return "";
            // TODO: 필요시 다른 노드(codeBlock, Iframe 등) 타입 추가
            default:
                // 알 수 없는 노드 타입의 경우, 내부 콘텐츠만 그대로 반환합니다.
                return innerContent;
        }
    }

    /**
     * 노드의 속성(attrs) 맵을 기반으로 HTML style 문자열을 생성합니다.
     * @param attrs 노드의 속성 맵
     * @return " style=\"...\"" 형태의 문자열 또는 빈 문자열
     */
    private String buildStyleString(Map<String, Object> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return "";
        }

        StringBuilder styleBuilder = new StringBuilder();
        
        // Helper to safely append styles
        java.util.function.Consumer<String> appendStyle = (key) -> {
            if (attrs.containsKey(key) && attrs.get(key) != null) {
                String cssKey = key.equals("textAlign") ? "text-align"
                            : key.equals("backgroundColor") ? "background-color"
                            : key; // Convert camelCase to kebab-case
                styleBuilder.append(cssKey).append(": ").append(attrs.get(key)).append(";");
            }
        };

        appendStyle.accept("textAlign");
        appendStyle.accept("backgroundColor");
        appendStyle.accept("fontFamily");
        appendStyle.accept("width");
        appendStyle.accept("height");
        // TODO: 필요한 다른 CSS 속성들을 여기에 추가

        if (styleBuilder.length() > 0) {
            return " style=\"" + HtmlUtils.htmlEscape(styleBuilder.toString()) + "\"";
        }
        
        return "";
    }
} 