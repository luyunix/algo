# EF001 - 寻找旋转排序数组中的最小值 题解

## 无重复元素版本

```java
public int findMin(int[] nums) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] > nums[right]) left = mid + 1;
        else right = mid;
    }
    return nums[left];
}
```

## 有重复元素版本

```java
public int findMinWithDuplicates(int[] nums) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] > nums[right]) left = mid + 1;
        else if (nums[mid] < nums[right]) right = mid;
        else right--;
    }
    return nums[left];
}
```

## 执行过程图解（无重复）

### Case 1: 标准旋转数组

**输入**: nums = [4, 5, 6, 7, 0, 1, 2]

```
初始: left=0, right=6

第1轮:
[4, 5, 6, 7, 0, 1, 2]
 L        M        R   mid = 0 + (6-0)/2 = 3
          ↑            nums[3] = 7, nums[6] = 2
                       7 > 2，说明最小值在右边
                       left = mid + 1 = 4

第2轮:
[4, 5, 6, 7, 0, 1, 2]
                L  M  R   mid = 4 + (6-4)/2 = 5
                   ↑       nums[5] = 1, nums[6] = 2
                           1 < 2，说明最小值在左边（含mid）
                           right = mid = 5

第3轮:
[4, 5, 6, 7, 0, 1, 2]
                L  R      mid = 4 + (5-4)/2 = 4
                M          nums[4] = 0, nums[5] = 1
                ↑          0 < 1，最小值在左边（含mid）
                           right = mid = 4

第4轮:
[4, 5, 6, 7, 0, 1, 2]
                L=R=4     left == right，结束

返回 nums[4] = 0 ✓
```

### Case 2: 未旋转（边界）

**输入**: nums = [1, 2, 3, 4, 5]

```
初始: left=0, right=4

第1轮:
[1, 2, 3, 4, 5]
 L     M     R    mid = 0 + (4-0)/2 = 2
       ↑           nums[2] = 3, nums[4] = 5
                   3 < 5，最小值在左边（含mid）
                   right = mid = 2

第2轮:
[1, 2, 3, 4, 5]
 L  M  R          mid = 0 + (2-0)/2 = 1
    ↑              nums[1] = 2, nums[2] = 3
                   2 < 3，最小值在左边（含mid）
                   right = mid = 1

第3轮:
[1, 2, 3, 4, 5]
 L=M=R=0           mid = 0 + (1-0)/2 = 0
 ↑                 nums[0] = 1, nums[1] = 2
                   1 < 2，最小值在左边（含mid）
                   right = mid = 0

第4轮:
[1, 2, 3, 4, 5]
 L=R=0             left == right，结束

返回 nums[0] = 1 ✓ (第一个元素就是最小值)
```

### Case 3: 单元素（边界）

**输入**: nums = [1]

```
初始: left=0, right=0

直接 left == right

返回 nums[0] = 1 ✓
```

### Case 4: 两个元素旋转（边界）

**输入**: nums = [2, 1]

```
初始: left=0, right=1

第1轮:
[2, 1]
 L  R             mid = 0 + (1-0)/2 = 0
 M                 nums[0] = 2, nums[1] = 1
 ↑                 2 > 1，最小值在右边
                   left = mid + 1 = 1

第2轮:
[2, 1]
 L=R=1            left == right，结束

返回 nums[1] = 1 ✓
```

## 执行过程图解（有重复）

### Case 1: 有重复，最小值在中间

**输入**: nums = [2, 2, 2, 0, 1]

```
初始: left=0, right=4

第1轮:
[2, 2, 2, 0, 1]
 L     M     R    mid = 0 + (4-0)/2 = 2
       ↑           nums[2] = 2, nums[4] = 1
                   2 > 1，最小值在右边
                   left = mid + 1 = 3

第2轮:
[2, 2, 2, 0, 1]
          L  M  R   mid = 3 + (4-3)/2 = 3
             ↑       nums[3] = 0, nums[4] = 1
                   0 < 1，最小值在左边（含mid）
                   right = mid = 3

第3轮:
[2, 2, 2, 0, 1]
          L=R=3     left == right，结束

返回 nums[3] = 0 ✓
```

### Case 2: 全是重复（边界）

**输入**: nums = [2, 2, 2, 2, 2]

```
初始: left=0, right=4

第1轮:
[2, 2, 2, 2, 2]
 L     M     R    mid = 0 + (4-0)/2 = 2
       ↑           nums[2] = 2, nums[4] = 2
                   相等，无法判断，缩小范围
                   right-- = 3

第2轮:
[2, 2, 2, 2, 2]
 L     M  R       mid = 0 + (3-0)/2 = 1
    ↑              nums[1] = 2, nums[3] = 2
                   相等，无法判断
                   right-- = 2

第3轮:
[2, 2, 2, 2, 2]
 L  M  R          mid = 0 + (2-0)/2 = 1
    ↑              nums[1] = 2, nums[2] = 2
                   相等，无法判断
                   right-- = 1

第4轮:
[2, 2, 2, 2, 2]
 L=R=0            left == right，结束

返回 nums[0] = 2 ✓
```

### Case 3: 首尾重复，最小值在中间

**输入**: nums = [1, 1, 1, 0, 1]

```
初始: left=0, right=4

第1轮:
[1, 1, 1, 0, 1]
 L     M     R    mid = 0 + (4-0)/2 = 2
       ↑           nums[2] = 1, nums[4] = 1
                   相等，无法判断
                   right-- = 3

第2轮:
[1, 1, 1, 0, 1]
 L     M  R       mid = 0 + (3-0)/2 = 1
    ↑              nums[1] = 1, nums[3] = 0
                   1 > 0，最小值在右边
                   left = mid + 1 = 2

第3轮:
[1, 1, 1, 0, 1]
       L  R        mid = 2 + (3-2)/2 = 2
       M           nums[2] = 1, nums[3] = 0
       ↑           1 > 0，最小值在右边
                   left = mid + 1 = 3

第4轮:
[1, 1, 1, 0, 1]
          L=R=3    left == right，结束

返回 nums[3] = 0 ✓
```

## 核心区别

| 情况                         | 无重复              | 有重复              |
|----------------------------|------------------|------------------|
| `nums[mid] > nums[right]`  | `left = mid + 1` | `left = mid + 1` |
| `nums[mid] < nums[right]`  | `right = mid`    | `right = mid`    |
| `nums[mid] == nums[right]` | 不可能发生            | `right--`        |
