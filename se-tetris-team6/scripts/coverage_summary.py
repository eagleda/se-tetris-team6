#!/usr/bin/env python3
"""
Jacoco XML 리포트에서
- 클래스별 LINE 커버리지%
- 미싱 라인 수

를 뽑아서, 커버리지가 낮은 순으로 정렬해 출력하는 스크립트.

사용 예:
    python scripts/coverage_summary.py \
        app/build/reports/jacoco/test/jacocoTestReport.xml \
        --threshold 0.7 \
        --limit 50
"""

import argparse
import xml.etree.ElementTree as ET
from typing import List, Tuple


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "xml_path",
        help="jacocoTestReport.xml 경로 (예: app/build/reports/jacoco/test/jacocoTestReport.xml)",
    )
    parser.add_argument(
        "--threshold",
        type=float,
        default=0.7,
        help="이 값 이하 커버리지만 출력 (0.0~1.0, 기본=0.7 -> 70%% 이하)",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=0,
        help="출력 개수 제한 (0이면 제한 없음)",
    )
    return parser.parse_args()


def extract_class_coverage(root: ET.Element) -> List[Tuple[float, int, int, str]]:
    """
    Jacoco XML 루트에서 클래스별 LINE 커버리지 정보를 뽑아온다.

    반환: (coverage_ratio, missed, covered, fqn)
    """
    rows: List[Tuple[float, int, int, str]] = []

    for pkg in root.findall("package"):
        pkg_name = pkg.get("name", "").replace("/", ".")
        for clazz in pkg.findall("class"):
            cls_name = clazz.get("name", "").replace("/", ".")
            # JaCoCo class name은 보통 "tetris/view/GameComponent" 형태라 .class 확장자는 없음
            fqn = f"{pkg_name}.{cls_name}" if pkg_name else cls_name

            line_counter = None
            for counter in clazz.findall("counter"):
                if counter.get("type") == "LINE":
                    line_counter = counter
                    break

            if line_counter is None:
                # LINE 정보 없는 클래스는 스킵
                continue

            missed = int(line_counter.get("missed", "0"))
            covered = int(line_counter.get("covered", "0"))
            total = missed + covered
            if total == 0:
                # 실행된 적 없는 클래스 (테스트 대상이 아니거나 빈 껍데기)
                continue

            coverage_ratio = covered / total
            rows.append((coverage_ratio, missed, covered, fqn))

    return rows


def main() -> None:
    args = parse_args()

    tree = ET.parse(args.xml_path)
    root = tree.getroot()

    rows = extract_class_coverage(root)

    # 커버리지 낮은 순으로 정렬
    rows.sort(key=lambda x: x[0])

    printed = 0
    print("# coverage <= {:.1f}% 인 클래스 목록".format(args.threshold * 100))
    print("# 형식: <coverage%>\tmissed=<N>\tcovered=<N>\t<fully-qualified-class-name>")
    print()

    for coverage_ratio, missed, covered, fqn in rows:
        if coverage_ratio > args.threshold:
            # threshold보다 높은 커버리지는 출력하지 않음
            continue

        print("{:5.1f}%\tmissed={:4d}\tcovered={:4d}\t{}".format(
            coverage_ratio * 100, missed, covered, fqn
        ))
        printed += 1
        if args.limit and printed >= args.limit:
            break

    if printed == 0:
        print("# threshold 이하인 클래스가 없습니다.")


if __name__ == "__main__":
    main()