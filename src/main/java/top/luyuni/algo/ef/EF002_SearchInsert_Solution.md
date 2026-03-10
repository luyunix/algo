# EF002 - 搜索插入位置 题解

## 代码

```java
public int searchInsert(int[] nums, int target) {
    int left = 0, right = nums.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] >= target) right = mid;
        else left = mid + 1;
    }
    return left;
}
```

## 执行过程图解

### Case 1: target 在数组中

**输入**: nums = [1, 3, 5, 6], target = 5

```
初始: left=0, right=4

第1轮:
[1, 3, 5, 6]
 L     M   R      mid = 0 + (4-0)/2 = 2
       ↑           nums[2] = 5 >= 5 ✓
                  满足条件，往左找: right = mid = 2

第2轮:
[1, 3, 5, 6]
 L  M  R          mid = 0 + (2-0)/2 = 1
    ↑              nums[1] = 3 >= 5 ✗
                  不满足，往右找: left = mid + 1 = 2

第3轮:
[1, 3, 5, 6]
 L=R=2            left == right，结束

返回 left = 2 ✓
```

### Case 2: target 不在，插入中间

**输入**: nums = [1, 3, 5, 6], target = 2

```
初始: left=0, right=4

第1轮:
[1, 3, 5, 6]
 L     M   R      mid = 2, nums[2] = 5 >= 2 ✓
       ↑           right = 2

第2轮:
[1, 3, 5, 6]
 L  M  R          mid = 1, nums[1] = 3 >= 2 ✓
    ↑              right = 1

第3轮:
[1, 3, 5, 6]
 L=M=R=0          mid = 0, nums[0] = 1 >= 2 ✗
 ↑                left = mid + 1 = 1

第4轮:
[1, 3, 5, 6]
 L=R=1            left == right，结束

返回 left = 1 ✓
```

### Case 3: target 比所有元素大（边界）

**输入**: nums = [1, 3, 5, 6], target = 7

```
初始: left=0, right=4

第1轮:
[1, 3, 5, 6]
 L     M   R      mid=2, nums[2]=5 >= 7 ✗
       ↑           left = mid + 1 = 3

第2轮:
[1, 3, 5, 6]
          L   R    mid=3, nums[3]=6 >= 7 ✗
          M        left = mid + 1 = 4
          ↑

第3轮:
[1, 3, 5, 6]
              L=R=4   left == right，结束

返回 left = 4 ✓ (插入数组末尾)
```

**关键**: right 初始为 nums.length，所以 left 最大可以到 nums.length

### Case 4: target 比所有元素小（边界）

**输入**: nums = [1, 3, 5, 6], target = 0

```
初始: left=0, right=4

第1轮:
[1, 3, 5, 6]
 L     M   R      mid=2, nums[2]=5 >= 0 ✓
       ↑           right = mid = 2

第2轮:
[1, 3, 5, 6]
 L  M  R          mid=1, nums[1]=3 >= 0 ✓
    ↑              right = mid = 1

第3轮:
[1, 3, 5, 6]
 L=M=R=0          mid=0, nums[0]=1 >= 0 ✓
 ↑                right = mid = 0

第4轮:
[1, 3, 5, 6]
 L=R=0            left == right，结束

返回 left = 0 ✓ (插入数组开头)
```

### Case 5: 空数组（边界）

**输入**: nums = [], target = 5

```
初始: left=0, right=0

直接满足 left == right

返回 left = 0 ✓
```

## 为什么 right = nums.length？

| right 取值        | 能否处理 target 比所有元素大         |
|-----------------|----------------------------|
| nums.length - 1 | ❌ 不能，最大只能返回 nums.length-1  |
| nums.length     | ✓ 能，可以返回 nums.length（插入末尾） |

## 总结

```
找左边界模板：
- right = nums.length（右开，可以取到数组长度）
- nums[mid] >= target 时，right = mid（满足条件往左）
- 最后返回 left
```
