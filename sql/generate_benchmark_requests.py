"""Generate deterministic interval requests for the 60 bays in 07_dsa_demo.sql."""

import argparse
import csv
from datetime import datetime, timedelta
from pathlib import Path
from random import Random


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    parser.add_argument("--count", type=int, default=100_000)
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args()
    if args.count <= 0:
        parser.error("--count must be positive")

    random = Random(args.seed)
    lots = ["DSA Riverside", "DSA Central", "DSA West"]
    baseline = datetime(2030, 1, 1)
    with args.output.open("x", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(["request_id", "lot_name", "bay_number", "start_time", "end_time"])
        for request_id in range(1, args.count + 1):
            lot = random.choice(lots)
            bay = f"{random.randint(1, 2)}-{random.randint(1, 10):02d}"
            start = baseline + timedelta(minutes=15 * random.randrange(30 * 24 * 4))
            end = start + timedelta(minutes=random.choice([15, 30, 60, 120]))
            writer.writerow([request_id, lot, bay, start.isoformat(sep=" "), end.isoformat(sep=" ")])


if __name__ == "__main__":
    main()
