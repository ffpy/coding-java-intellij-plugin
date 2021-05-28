package org.ffpy.plugin.coding.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 注释位置
 */
@RequiredArgsConstructor
public enum CommentPosition {
    UP("节点上方",
            e -> e.selectNodes("./preceding-sibling::comment()[1]"),
            n -> n.selectNodes("./following-sibling::*")),
    DOWN("节点下方",
            e -> e.selectNodes("./following-sibling::comment()[1]"),
            n -> n.selectNodes("./preceding-sibling::*")),
    NONE("无", e -> Collections.emptyList(), n -> Collections.emptyList()),
    ;
    @Getter
    private final String name;
    private final Function<Element, List<Node>> finder;
    private final Function<Node, List<Node>> prev;

    /**
     * 获取标签对应的注释节点
     *
     * @param el 标签
     * @return 注释节点
     */
    public Optional<Node> findComment(Element el) {
        return finder.apply(el).stream()
                .filter(node -> findNodeByComment(node) == el)
                .findFirst();
    }

    /**
     * 获取注释节点对应的标签
     *
     * @param node 注释节点
     * @return 标签
     */
    private Node findNodeByComment(Node node) {
        List<Node> list = prev.apply(node);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
}
