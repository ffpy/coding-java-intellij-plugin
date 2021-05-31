package org.ffpy.plugin.coding.constant

import org.dom4j.Element
import org.dom4j.Node

/**
 * 注释位置
 */
enum class CommentPosition(
    val text: String,
    private val findCommentByNodeAction: (Node) -> List<Node>,
    private val findNodeByCommentAction: (Node) -> List<Node>
) {
    UP("节点上方",
        { it.selectNodes("./preceding-sibling::comment()[1]") },
        { it.selectNodes("./following-sibling::*") }),

    DOWN("节点下方",
        { it.selectNodes("./following-sibling::comment()[1]") },
        { it.selectNodes("./preceding-sibling::*") }),

    NONE("无",
        { emptyList() },
        { emptyList() }),
    ;

    /**
     * 获取标签对应的注释节点
     *
     * @param el 标签
     * @return 注释节点
     */
    fun findComment(el: Element): Node? {
        return findCommentByNodeAction(el).firstOrNull()?.let {
            if (findNodeByComment(it) === el) it else null
        }
    }

    /**
     * 获取注释节点对应的标签
     *
     * @param node 注释节点
     * @return 标签
     */
    private fun findNodeByComment(node: Node): Node? {
        val list = findNodeByCommentAction(node)
        return if (list.isEmpty()) null else list[list.size - 1]
    }
}