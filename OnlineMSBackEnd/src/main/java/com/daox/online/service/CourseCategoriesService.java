package com.daox.online.service;

import com.daox.online.entity.mysql.CourseCategories;
import com.daox.online.entity.views.responseVO.CategoryVo;
import com.daox.online.mapper.CourseCategoryMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseCategoriesService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 获取树形的分类结构
     *
     * @return 嵌套的分类列表
     */
    public List<CategoryVo> getCategoryTree() {
        // 1. 从数据库一次性查询出所有的分类
        List<CourseCategories> allCategories = courseCategoryMapper.findAll();

        // 2. 将扁平列表转换为 CategoryVo 列表，并创建一个Map以便快速查找
        Map<String, CategoryVo> categoryMap = allCategories.stream()
                .map(category -> {
                    CategoryVo vo = new CategoryVo();
                    BeanUtils.copyProperties(category, vo); // 属性拷贝
                    return vo;
                })
                .collect(Collectors.toMap(CategoryVo::getId, Function.identity()));

        // 3. 组装树形结构
        List<CategoryVo> rootNodes = new ArrayList<>();

        for (CategoryVo node : categoryMap.values()) {
            // 判断是否为根节点 (parentId为"0"或不存在于Map中)
            String parentId = node.getParentId();
            if ("0".equals(parentId) || !categoryMap.containsKey(parentId)) {
                rootNodes.add(node);
            } else {
                // 如果是子节点，就从Map中找到它的父节点，并把自己加进去
                CategoryVo parentNode = categoryMap.get(parentId);
                parentNode.getChildren().add(node);
            }
        }

        return rootNodes;
    }

}
