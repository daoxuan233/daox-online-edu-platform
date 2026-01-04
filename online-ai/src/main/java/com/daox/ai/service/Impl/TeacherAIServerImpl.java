package com.daox.ai.service.Impl;

import com.daox.ai.entity.CourseOutlineDto;
import com.daox.ai.entity.ms.Question;
import com.daox.ai.entity.request.QuestionGenerationRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TeacherAIServerImpl {

    @Resource
    private ChatClient chatClient;

    @Autowired
    @Qualifier("dashScopeChatClient")
    private ChatClient chatClientDashScope;

    @Resource
    private VectorStore vectorStore; // 用于语义查重

    @Resource
    private StringRedisTemplate stringRedisTemplate; // 用于MD5布隆/精准去重

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
     * AI智能出题助手 (优化版：去重 + 负采样 + 动态阈值)
     *
     * @param request 请求
     * @return 问题
     */
    /*public Question optimizationAssistantQuestions(QuestionGenerationRequest request) {

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
    }*/
    public Question optimizationAssistantQuestions(QuestionGenerationRequest request) {
        // 最大重试次数，防止无限循环
        int maxRetries = 3;
        int attempt = 0;

        // 1. 准备初始 Prompt (加入“负采样”提示)
        // 注意：这里我们使用一个可变的 String，因为重试时需要修改它
        String currentPromptContent = buildPrompt(request);

        while (attempt < maxRetries) {
            attempt++;
            try {
                // 2. 调用 AI 生成题目
                Question generatedQuestion = chatClient.prompt(new Prompt(currentPromptContent))
                        .call()
                        .entity(Question.class);

                // 3. 执行双重去重检查
                if (isUniqueQuestion(generatedQuestion)) {
                    // 4. 如果唯一，则入库(更新查重指纹)并返回
                    saveQuestionFingerprints(generatedQuestion);
                    return generatedQuestion;
                } else {
                    log.info("检测到重复题目，正在进行第 {} 次重试...", attempt);
                    // --- 【核心优化】动态微调 Prompt ---
                    // 只有当还有剩余重试次数时才进行 Prompt 修改
                    if (attempt < maxRetries) {
                        currentPromptContent = injectRetryInstruction(currentPromptContent, attempt);
                    }
                }
            } catch (Exception e) {
                log.error("AI 生成异常: {}", e.getMessage());
            }
        }

        throw new RuntimeException("AI 尝试 " + maxRetries + " 次仍未生成原创题目，请稍后重试。");
    }

    /**
     * 构建 Prompt (步骤 2: 负采样优化)
     */
    private String buildPrompt(QuestionGenerationRequest request) {
        String formattedOutline = formatOutlineForPrompt(request.getCourse().getOutline());

        // 新增：负采样提示 (Negative Prompting)
        String negativeConstraints = """
                # ⛔ 负面约束 (Negative Constraints) - 绝对禁止以下行为：
                1.  **禁止重复:** 不要直接复制课程大纲中的原话作为题干。
                2.  **禁止模糊:** 题干不能包含“如下”、“下列”等指代不明的词，除非选项就在下方。
                3.  **禁止常识性错误:** 错误选项(Distractors)必须具有迷惑性，但不能在逻辑上明显荒谬。
                4.  **禁止泄露:** 解析字段不要简单重复答案，必须解释“为什么”。
                """;

        String template = """
                # Role / 角色设定
                您是【{categoryName}】领域的殿堂级技术专家与教育家。您深知“高难度”不等于“生僻怪题”，而是指**对核心原理的深度综合应用**。您擅长设计那种需要通过多步逻辑推演、排除干扰项后才能触达真相的“长思维链”试题。
                
                # Goal / 目标
                基于【课程大纲】，为课程《{courseTitle}》设计一道**{difficulty}**难度的**{questionType}**。
                
                # Context / 背景资料
                **课程大纲:**
                {courseOutline}
                
                # Workflow / 出题思维链 (必须执行)
                在生成 JSON 前，请按以下逻辑进行深度推演：
                1.  **锚定核心**: 从大纲中选取一个核心知识点 A。
                2.  **难度注入 (根据 `{difficulty}` 执行不同策略)**:
                    * 若为 **EASY**: 直接考察 A 的定义或基础用法。
                    * 若为 **MEDIUM**: 设定一个典型场景，考察 A 的实际应用或代码逻辑。
                    * **若为 HARD (深度综合)**:
                        * **跨点融合**: 选取大纲中的另一个关联知识点 B，在引入一个系统约束 C (如内存限制、多线程环境、异常边界)或则引入一个边界条件 D。
                        * **构建陷阱**: 设计一个符合直觉但错误的陷阱（Trap），只有深入理解 A 和 B 与 C 或 D 的交互机制才能避开。
                        * **长链推理**: 题目不应一步得出答案，必须要求考生先推导出中间状态，再得出最终结论。
                3.  **最终校验**: 确保逻辑闭环，HARD 题目的解析必须包含推导过程。
                
                # Constraints / 核心约束规则
                1.  **难度严格对标**:
                    * **EASY**: 【记忆/复现】单一知识点的直白考察。
                    * **MEDIUM**: 【理解/应用】代码片段分析、单一逻辑的调试或重构。
                    * **HARD**: 【分析/评估】**必须满足以下特征**：
                        1.  **综合性**: 至少涉及 3 个以上不同维度的知识点（如“集合源码”+“并发安全”）。
                        2.  **深度扩展**: 考察底层原理（如内存模型、字节码、硬件交互）而非表面 API。
                        3.  **反直觉**: 答案往往与表面直觉相反，需要深层逻辑支撑。
                2.  **题型与字段规范 (必须严格遵守)**:
                    * `stem` (题干):
                        * **HARD 题干要求**: 必须构建一个具体的、复杂的工程场景或代码上下文，不能少于 50 字。
                        * **填空题**: 使用 `___` (三个下划线) 作为占位符。
                    * `options` (选项):
                        * **`SINGLE_CHOICE` / `MULTI_CHOICE`**: 4 个选项。干扰项必须基于常见的思维误区设计。
                        * **`TRUE_FALSE`**: 固定为 `[{ "key": "A", "text": "正确" }, { "key": "B", "text": "错误" }]`。
                        * 其他题型设为 `[]`。
                    * `answer` (答案): 格式必须符合 JSON 类型要求 (字符串或数组)。
                    * `analysis` (解析):
                        * **HARD 解析要求**: 必须分步骤解释。第一步分析场景，第二步推导原理，第三步解释为何其他选项错误（错误原因分析）。
                    * `tags` (标签): 提取 3-5 个关键词，HARD 题必须包含底层原理相关的标签。
                3.  **禁止事项**:
                    * 禁止考察过时 10 年以上的陈旧技术。
                    * 禁止输出 Markdown 标记（如 ```json），**只输出纯 JSON 字符串**。
                
                # Format Output / 输出格式
                请直接填充以下 JSON 结构，不要包含任何前缀或后缀文本：
                {format}
                """;

        // 插入负面约束
        String promptString = String.format(template, negativeConstraints);

        return promptString
                .replace("{categoryName}", request.getCourse().getCategory())
                .replace("{courseTitle}", request.getCourse().getTitle())
                .replace("{questionType}", request.getType().name())
                .replace("{difficulty}", request.getDifficulty().name())
                .replace("{courseOutline}", formattedOutline);
    }

    /**
     * 核心去重逻辑
     */
    private boolean isUniqueQuestion(Question q) {
        String stem = q.getStem();
        if (stem == null || stem.isBlank()) return false;

        // --- 第一道防线：MD5 哈希比对 (模拟布隆过滤器) ---
        // 计算题干 MD5
        String stemHash = DigestUtils.md5DigestAsHex(stem.getBytes(StandardCharsets.UTF_8));
        String hashKey = "bloom:question:md5:" + stemHash;

        // 如果 Redis 中已存在该 Hash，说明完全重复 (这里用 hasKey 模拟布隆过滤器)
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(hashKey))) {
            log.info("【去重拦截】MD5哈希命中，完全重复: {}", stem);
            return false;
        }

        // --- 第二道防线：向量语义相似度 (步骤 3: 动态阈值) ---
        // 阈值设定为 0.8
        double similarityThreshold = 0.8;

        // 构建搜索请求：只找最相似的1条
        SearchRequest searchRequest = SearchRequest.builder()
                .query(stem) // 搜索题干
                .topK(1) // 只找最相似的1条
                .similarityThreshold(similarityThreshold) // 设置相似度阈值
                .build();

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        if (!similarDocs.isEmpty()) {
            // 如果能搜出来，说明相似度 >= 0.8 (Spring AI 的 threshold 是过滤下限)
            Document match = similarDocs.getFirst();
            log.info("【去重拦截】语义相似度过高。库中原题: {}", match.getText());
            return false; // 重复
        }

        return true; // 通过所有检查
    }

    /**
     * 辅助方法：注入重试指令
     * 原理：找到 Prompt 结尾的 {format} 占位符，在其前方插入针对性的重试指令
     */
    private String injectRetryInstruction(String originalPrompt, int currentAttempt) {
        String retryInstruction = "";

        // 根据重试次数，施加不同程度的“压力”
        if (currentAttempt == 1) {
            retryInstruction = """
                    
                    # ⚠️ 警告：检测到重复 (Retry Strategy - Level 1)
                    您上一次生成的题目与现有题库**重复**。
                    **修正指令：**
                    1. 请保持难度不变，但务必**更换一个具体的切入点**。
                    2. 如果是代码题，请改变**变量名**和**业务场景**（例如从“订单系统”改为“图书管理”）。
                    3. 如果是概念题，请考察该知识点的**反面**或**例外情况**。
                    """;
        } else if (currentAttempt == 2) {
            retryInstruction = """
                    
                    # ⛔ 严重警告：再次重复 (Retry Strategy - Level 2)
                    连续两次生成重复题目！这是不可接受的。
                    **强制指令：**
                    1. 立即放弃当前选定的知识点细节，**选取大纲中同章节下的另一个冷门知识点**。
                    2. 必须构建一个**完全陌生**的场景。
                    3. 请使用**逆向思维**出题。
                    """;
        }

        // 将指令插入到 {format} 之前，确保指令被 AI 读取，且不破坏 JSON 格式化
        // 假设 buildPrompt 返回的字符串结尾包含 "{format}"
        if (originalPrompt.contains("{format}")) {
            return originalPrompt.replace("{format}", retryInstruction + "\n{format}");
        } else {
            // 如果找不到占位符（防御性编程），直接追加在末尾
            return originalPrompt + "\n" + retryInstruction;
        }
    }

    /**
     * 保存题目指纹 (入库)
     * 注意：这步只保存指纹用于后续查重，真正的业务入库在Controller层处理
     */
    private void saveQuestionFingerprints(Question q) {
        String stem = q.getStem();

        // 1. 保存 MD5 (用于精确查重)
        String stemHash = DigestUtils.md5DigestAsHex(stem.getBytes(StandardCharsets.UTF_8));
        // 设置过期时间防止无限膨胀，例如 30 天
        stringRedisTemplate.opsForValue().set("bloom:question:md5:" + stemHash, "1", 30, TimeUnit.DAYS);

        // 2. 保存向量 (用于语义查重)
        // 注意：这里需要将 Question 转为 Document。
        // 元数据根据需要添加
        Document doc = new Document(stem, Map.of("type", q.getType().name()));
        vectorStore.add(List.of(doc));
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
