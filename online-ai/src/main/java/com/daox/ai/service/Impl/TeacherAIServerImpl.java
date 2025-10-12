package com.daox.ai.service.Impl;

import com.daox.ai.entity.CourseOutlineDto;
import com.daox.ai.entity.ms.Question;
import com.daox.ai.entity.request.QuestionGenerationRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TeacherAIServerImpl {

    @Resource
    private ChatClient chatClient;

    @Autowired
    @Qualifier("dashScopeChatClient")
    private ChatClient chatClientDashScope;

    /**
     * 一个教师关于课程简介填写AI优化助手
     *
     * @param question 教师填写的课程简介
     * @return 优化后的课程简介
     */
    public Flux<String> optimizationAssistant(String question) {
        final String systemPrompt = """
                你是一个专业的课程设计助手，专门帮助教师优化课程简介。
                你的任务是根据教师提供的初步想法或则标题，生成吸引人的课程简介。
                简介都应该包含：
                1.  一个引人入胜的开场白，激发学生的兴趣。
                2.  清晰的学习要点，告诉学生他们将学到什么。
                3.  一个明确的结尾，鼓励学生报名。
                
                请确保简介风格专业严谨、活泼有趣、强调实践价值，并且语言流畅、没有错别字。
                请直接返回优化后的简介内容，不要包含额外的解释或对话。
                请确保简介长度在100到185个字符之间，尽可能使用markdown格式。
                """;
        return chatClient
                .prompt()
                .user(question)
                .system(systemPrompt)
                .stream()
                .content();
    }

    /**
     * AI助手 - 适合人群
     *
     * @param question 课程主题
     * @return 适合人群
     */
    public Flux<String> optimizationAssistantCrowd(String question) {
        final String systemPrompt = """
                你是一位经验丰富的教学设计师，擅长为课程精准地打上“适合人群”的标签。
                你的任务是根据教师提供的课程主题，生成一组描述目标学员的关键词或短语。
                请遵循以下指导原则：
                1.  **精准**: 标签应直接反映目标学员的身份、水平或需求（例如：编程初学者, 在校大学生, 产品经理）。
                2.  **简洁**: 每个标签应简短明了。
                3.  **全面**: 覆盖所有主要的目标学员群体。
                
                你的输出必须是一个单行的、由英文逗号（,）分隔的字符串。
                绝对不要使用任何编号、项目符号、换行符或其他格式。
                例如，对于“Java入门课程”，一个好的输出是：“零基础学员,计算机专业学生,Java爱好者,希望转行程序员的人”。
                """;


        return chatClient
                .prompt()
                .user(question)
                .system(systemPrompt)
                .stream()
                .content();
    }

    /**
     * AI助手 - 技术要求
     *
     * @param question 问题
     * @return “技术要求”清单
     */
    public Flux<String> optimizationAssistantRequirements(String question) {
        final String systemPrompt = """
                你是一位严谨的课程顾问，负责为课程生成清晰的“技术要求”或“学习前提”标签。
                你的任务是根据教师提供的课程内容，生成一组学习该课程所需具备的技能、软件或硬件条件。
                请遵循以下指导原则：
                1.  **具体**: 明确指出所需的技术或工具，如果涉及软件，最好包含版本建议（例如：Java 11+, Maven 3.6+）。
                2.  **核心**: 只列出最关键和必要的要求。
                3.  **简洁**: 每个要求都应是一个简短的词组。
                
                你的输出必须是一个单行的、由英文逗号（,）分隔的字符串。
                绝对不要使用任何编号、项目符号、换行符或其他格式。
                例如，对于“Spring Boot实战课程”，一个好的输出是：“熟悉Java语言,了解Spring框架基础,已安装IntelliJ IDEA,熟悉Git”。
                """;
        return chatClient
                .prompt()
                .user(question)
                .system(systemPrompt)
                .stream()
                .content();
    }

    /**
     * AI智能出题助手
     *
     * @param request 请求
     * @return 问题
     */
    public Question optimizationAssistantQuestions(QuestionGenerationRequest request) {

        // 将课程大纲格式化为适合放入 Prompt 的字符串
        String formattedOutline = formatOutlineForPrompt(request.getCourse().getOutline());

        // 步骤 2: 设计包含 {format} 占位符的提示模板。
        // 这个模板与之前相同，但我们处理它的方式将完全不同。
        String promptString = """
                # 角色
                您是一位专注于“{categoryName}”领域的资深出题专家。

                # 任务
                为课程“{courseTitle}”创作一道高质量的 `{questionType}` 考题，难度为 `{difficulty}`。

                # 背景资料
                **课程大纲:**
                {courseOutline}

                # 出题核心指令
                1.  **内容与结构:** 题目设计必须严格依据**课程大纲**，并完全遵循 `{format}` 提供的JSON格式。
                2.  **字段规则 (根据 `{questionType}` 动态调整):**
                    *   `stem` (题干): 必须清晰、完整地描述问题。对于填空题，请使用 `___` 表示需要填写的空白处。
                    *   `options` (选项):
                        *   **适用题型:** `SINGLE_CHOICE`, `MULTI_CHOICE`, `TRUE_FALSE`。
                        *   **对于其他题型 (`SHORT_ANSWER`, `FILL_IN_BLANKS`, `PROGRAMMING`):** 此字段必须为 `null` 或空数组 `[]`。
                        *   **对于 `SINGLE_CHOICE` 和 `MULTI_CHOICE`:** 提供4个选项，包含正确选项和具有逻辑迷惑性的错误选项。
                        *   **对于 `TRUE_FALSE`:** 固定提供两个选项：`[{ "key": "A", "text": "正确" }, { "key": "B", "text": "错误" }]`。
                    *   `answer` (答案):
                        *   **`SINGLE_CHOICE`:** 提供唯一的正确选项的 `key` 值 (字符串, 例如: `"B"`)。
                        *   **`MULTI_CHOICE`:** 以JSON数组形式提供 **1到4个** 随机数量的正确选项的 `key` 值 (例如: `["A", "C"]`)。请确保正确答案的数量是不可预测的。
                        *   **`TRUE_FALSE`:** 提供正确选项的 `key` 值 (`"A"` 或 `"B"`)。
                        *   **`SHORT_ANSWER`:** 提供一段简洁、准确的参考答案文本 (字符串)。
                        *   **`FILL_IN_BLANKS`:** 以JSON数组形式提供所有空白处应填写的答案。即使只有一个空，也必须使用数组 (例如: `["答案一", "答案二"]`)。
                        *   **`PROGRAMMING`:** 提供一段完整、可运行的参考代码作为答案 (字符串)。
                    *   `analysis` (解析): 提供详尽解释，说明正确答案的理由。对于编程题，解释算法思路；对于简答题，提供背景知识。
                    *   `tags` (标签): 提供一个包含至少3个字符串的数组：2个为大纲中的知识点，1个为课程的核心关键词。
                3.  **输出要求:**
                    *   所有字段均为必填项，内容不可为空 (除非规则中明确允许为 `null`)。
                    *   最终响应必须是一个单一、完整且有效的JSON对象，禁止包含任何JSON格式之外的文本。

                # 输出示例 (这是一个 `SINGLE_CHOICE` 的例子，其他题型请遵循上述规则调整)
                ```json
                \\{
                  "stem": "在Java中，下列哪个关键字用于显式地调用父类的构造方法？",
                  "options": [
                    \\{ "key": "A", "text": "this()" \\},
                    \\{ "key": "B", "text": "super()" \\},
                    \\{ "key": "C", "text": "new()" \\},
                    \\{ "key": "D", "text": "parent()" \\}
                  ],
                  "answer": "B",
                  "analysis": "在Java中，`super()` 是专门用于在子类的构造方法中调用父类构造方法的关键字。`this()` 用于调用同一个类中的其他构造方法。",
                  "tags": ["构造方法", "继承", "super关键字", "java"]
                \\}
                ```

                {format}
                """;

        // PromptTemplate promptTemplate = new PromptTemplate(promptString);

        // 步骤 3: 准备模板需要的变量。
        // 注意：我们不再需要手动处理 'format' 变量。
        // 【核心修正】: 使用可变的 HashMap，而不是不可变的 Map.of()
        String finalPromptContent = promptString
                .replace("{categoryName}", request.getCourse().getCategory())
                .replace("{courseTitle}", request.getCourse().getTitle())
                .replace("{questionType}", request.getType().name())
                .replace("{difficulty}", request.getDifficulty().name())
                .replace("{courseOutline}", formattedOutline);

        // 步骤 3: 直接使用最终的字符串创建 Prompt 对象。
        Prompt prompt = new Prompt(finalPromptContent);

        // 步骤 4: 【核心】使用 Spring AI 最新的 Fluent API 进行调用和转换。
        // 1. .prompt(prompt):      使用我们创建的提示。
        // 2. .call():             执行对AI模型的调用。
        // 3. .entity(Question.class): 告诉Spring AI我们期望一个Question类型的对象。
        //                           框架会自动处理 {format} 的填充和响应JSON的解析。

        // 返回最终构建完成的 Question 对象。
        return chatClientDashScope.prompt(prompt)
                .call()
                .entity(Question.class);
    }

    /**
     * 辅助方法：将课程大纲对象格式化为易于AI阅读的字符串
     *
     * @param outlineDto 课程大纲数据传输对象
     * @return 格式化后的字符串
     */
    private String formatOutlineForPrompt(CourseOutlineDto outlineDto) {
        if (outlineDto == null || outlineDto.getOutline() == null) {
            return "课程大纲信息不可用。";
        }
        StringBuilder sb = new StringBuilder();
        for (CourseOutlineDto.ChapterDto chapter : outlineDto.getOutline()) {
            sb.append("章节 ").append(chapter.getOrderIndex())
                    .append(": ").append(chapter.getTitle()).append("\n");

            if (chapter.getSections() != null) {
                // 根据您的需求，这里只传递小节名称和顺序
                for (CourseOutlineDto.SectionDto section : chapter.getSections()) {
                    sb.append("  - 小节 ").append(section.getOrderIndex())
                            .append(": ").append(section.getTitle()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
