## 修改目标
- 将 `e:\fuyou-parent\paper\赋优售货机管理系统的设计与实现.html` 中所有带有文件系统/代码目录路径的内容替换为纯文字描述（如“manage 模块”“管理接口层”“首页接口层”“管理视图层”等），避免出现具体盘符与 `src/...` 等路径。
- 在完成文字化描述后，继续执行 DOCX 生成与图示扩展，并更新参考文献列表。

## 具体改动范围
- 中文摘要：删除诸如 `e:\fuyou-parent\fuyou-manage`、`src\api\manage`、`src\views\manage`、`src\api\home` 等路径字面，改为“后端的业务核心位于 manage 模块，前端的管理接口层与视图层构成完整业务闭环，首页接口承担数据聚合展示”。
- 正文章节：用“后端 manage 模块”“前端管理接口层（API）”“管理视图层（Views）”“首页接口层”等泛化术语替代所有具体路径表述；其余技术内容保持不变。
- 英文摘要与关键词：同样移除任何路径字面；保持术语一致性（如 manage module, management API layer, management views）。

## 后续交付步骤
1. 编辑 HTML，统一移除路径字面并替换为文字描述。
2. 生成三类图示（架构图、ER 图、时序图）并内嵌到 HTML 中的相应章节。
3. 使用 PowerShell 的 Word COM 自动化将 HTML 转为 `.docx`：
```
$src = "e:\fuyou-parent\paper\赋优售货机管理系统的设计与实现.html"
$dst = "e:\fuyou-parent\paper\赋优售货机管理系统的设计与实现.docx"
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open($src)
$wdFormatDocumentDefault = 16
$doc.SaveAs([ref]$dst, [ref]$wdFormatDocumentDefault)
$doc.Close()
$word.Quit()
```
4. 拓展参考文献列表，新增 JWT、Redis、Quartz、Vue3、MyBatis、RuoYi 与 IoT 资料条目，并统一为五号宋体、1.5 倍行距。
5. 版式核验与交付：在浏览器与 Word 中核验字体、字号、行距与目录占位，输出最终 `.docx` 与图示资源。

## 说明
- 本次改动不影响技术内容，仅将路径表述改为可读性更强的文字描述，便于学术论文规范与读者理解。

## 请确认
- 若同意上述计划，我将开始实施：先完成路径文字化改写，再生成图示、导出 DOCX 并扩展参考文献。