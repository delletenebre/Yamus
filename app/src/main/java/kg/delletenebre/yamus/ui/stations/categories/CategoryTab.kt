package kg.delletenebre.yamus.ui.stations.categories

class CategoryTab(val title: String, val tags: List<String>) {
    constructor(title: String, tag: String) : this(title, listOf(tag))
}