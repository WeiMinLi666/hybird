#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将Markdown转换为DOCX格式
需要安装: pip install markdown pypandoc

注意：如果pypandoc不可用，将使用markdown生成HTML格式
"""

import os
import sys
import subprocess

def convert_with_pandoc(md_file, docx_file, reference_doc=None):
    """使用pandoc转换"""
    try:
        import pypandoc

        # 检查pandoc是否已安装
        if not pypandoc.get_pandoc_version():
            print("Pandoc未安装，请先安装pandoc")
            return False

        # 构建转换参数
        extra_args = []
        if reference_doc and os.path.exists(reference_doc):
            extra_args.append(f'--reference-doc={reference_doc}')

        # 执行转换
        pypandoc.convert_file(
            md_file,
            'docx',
            outputfile=docx_file,
            extra_args=extra_args
        )

        print(f"成功转换: {md_file} -> {docx_file}")
        return True

    except ImportError:
        print("pypandoc未安装，尝试使用subprocess调用pandoc...")
        return convert_with_subprocess(md_file, docx_file, reference_doc)

def convert_with_subprocess(md_file, docx_file, reference_doc=None):
    """使用subprocess调用pandoc"""
    try:
        cmd = ['pandoc', md_file, '-o', docx_file]

        if reference_doc and os.path.exists(reference_doc):
            cmd.append(f'--reference-doc={reference_doc}')

        result = subprocess.run(cmd, capture_output=True, text=True)

        if result.returncode == 0:
            print(f"成功转换: {md_file} -> {docx_file}")
            return True
        else:
            print(f"转换失败: {result.stderr}")
            return False

    except FileNotFoundError:
        print("pandoc命令未找到，请先安装pandoc")
        print("安装方法:")
        print("  macOS: brew install pandoc")
        print("  Linux: sudo apt-get install pandoc 或 sudo yum install pandoc")
        print("  Windows: 从 https://pandoc.org/installing.html 下载安装")
        return False

def convert_to_html(md_file, html_file):
    """转换markdown为HTML（备用方案）"""
    try:
        import markdown

        with open(md_file, 'r', encoding='utf-8') as f:
            content = f.read()

        html = markdown.markdown(content, extensions=['tables', 'fenced_code'])

        # 添加HTML头部
        html_content = f"""
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>混合证书管理系统概要设计</title>
    <style>
        body {{
            font-family: "Microsoft YaHei", Arial, sans-serif;
            line-height: 1.6;
            margin: 40px;
            max-width: 1200px;
        }}
        h1 {{ color: #333; border-bottom: 2px solid #333; padding-bottom: 10px; }}
        h2 {{ color: #444; border-bottom: 1px solid #ccc; padding-bottom: 5px; margin-top: 30px; }}
        h3 {{ color: #555; margin-top: 25px; }}
        h4 {{ color: #666; }}
        table {{ border-collapse: collapse; width: 100%; margin: 20px 0; }}
        th, td {{ border: 1px solid #ddd; padding: 12px; text-align: left; }}
        th {{ background-color: #f2f2f2; font-weight: bold; }}
        tr:nth-child(even) {{ background-color: #f9f9f9; }}
        pre {{ background-color: #f5f5f5; padding: 15px; border-radius: 5px; overflow-x: auto; }}
        code {{ background-color: #f5f5f5; padding: 2px 5px; border-radius: 3px; }}
        pre code {{ background-color: transparent; padding: 0; }}
    </style>
</head>
<body>
{html}
</body>
</html>
"""

        with open(html_file, 'w', encoding='utf-8') as f:
            f.write(html_content)

        print(f"成功生成HTML: {md_file} -> {html_file}")
        print("提示: 您可以在浏览器中打开HTML文件，然后使用浏览器的打印功能保存为PDF或DOCX")
        return True

    except ImportError:
        print("markdown库未安装，请先安装: pip install markdown")
        return False

def main():
    md_file = '硕士论文概要设计.md'
    docx_file = '硕士论文概要设计.docx'
    reference_doc = 'reference.docx'

    # 检查markdown文件是否存在
    if not os.path.exists(md_file):
        print(f"错误: 文件 {md_file} 不存在")
        sys.exit(1)

    # 尝试转换为DOCX
    if convert_with_pandoc(md_file, docx_file, reference_doc):
        sys.exit(0)

    # 如果pandoc不可用，生成HTML作为备用
    html_file = '硕士论文概要设计.html'
    print("\n尝试生成HTML格式...")
    if convert_to_html(md_file, html_file):
        sys.exit(0)

    sys.exit(1)

if __name__ == '__main__':
    main()
