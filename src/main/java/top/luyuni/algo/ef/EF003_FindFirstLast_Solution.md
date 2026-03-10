# EF003 - 找 target 的左右边界 题解

## 代码

```java
public int[] searchRange(int[] nums, int target) {
    int left = findLeft(nums, target);
    if (left == -1) return new int[]{-1, -1};
    int right = findRight(nums, target);
    return new int[]{left, right};
}

// 找左边界：第一个 >= target 的位置
private int findLeft(int[] nums, int target) {
    int left = 0, right = nums.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] >= target) right = mid;
        else left = mid + 1;
    }
    if (left >= nums.length || nums[left] != target) return -1;
    return left;
}

// 找右边界：最后一个 <= target 的位置
private int findRight(int[] nums, int target) {
    int left = 0, right = nums.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] > target) right = mid;  // 注意这里是 >
        else left = mid + 1;                   // <= 时往右走
    }
    return left - 1;  // 第一个 > target 的位置 - 1
}
```

## 找左边界图解

**输入**: nums = [5, 7, 7, 8, 8, 10], target = 8

```
找第一个 >= 8 的位置

初始: left=0, right=6

第1轮:
[5, 7, 7, 8, 8, 10]
 L        M      R    mid=3, nums[3]=8 >= 8 ✓
          ↑            满足，往左: right=3

第2轮:
[5, 7, 7, 8, 8, 10]
 L     M  R           mid=1, nums[1]=7 >= 8 ✗
       ↑               不满足，往右: left=2

第3轮:
[5, 7, 7, 8, 8, 10]
    L  M  R            mid=2, nums[2]=7 >= 8 ✗
          ↑            不满足，往右: left=3

left=right=3
检查 nums[3]=8 == 8 ✓
返回 3
```

## 找右边界图解

**输入**: nums = [5, 7, 7, 8, 8, 10], target = 8

```
找第一个 > 8 的位置，然后 -1

初始: left=0, right=6

第1轮:
[5, 7, 7, 8, 8, 10]
 L        M      R    mid=3, nums[3]=8 > 8 ✗
          ↑            不满足（不是>），往右: left=4

第2轮:
[5, 7, 7, 8, 8, 10]
             L  M  R   mid=5, nums[5]=10 > 8 ✓
                ↑       满足，往左: right=5

第3轮:
[5, 7, 7, 8, 8, 10]
             L=R=4     mid=4, nums[4]=8 > 8 ✗
                      不满足，往右: left=5

left=right=5
返回 left - 1 = 4 ✓
```

## 关键区别

| 边界  | 条件                    | 满足时           | 不满足时             |
|-----|-----------------------|---------------|------------------|
| 左边界 | `nums[mid] >= target` | `right = mid` | `left = mid + 1` |
| 右边界 | `nums[mid] > target`  | `right = mid` | `left = mid + 1` |

**核心**: 左边界用 `>=`，右边界用 `>`

## 边界 case：全部相同

**输入**: nums = [2, 2, 2, 2, 2], target = 2

```
找左边界:
最终会找到 left=0, nums[0]=2 == 2 ✓
返回 0

找右边界:
找第一个 > 2 的位置
所有元素都 <= 2，所以 left 会一直往右走
最终 left=5（nums.length）
返回 left - 1 = 4 ✓
```

## 边界 case：target 不存在

**输入**: nums = [5, 7, 7, 8, 8, 10], target = 6

```
找左边界:
最终会找到 left=2（第一个 >= 6 的是 7）
检查 nums[2]=7 != 6
返回 -1
```

## 总结

```
左边界：找第一个 >= target
       条件用 >=，满足往左

右边界：找第一个 > target，然后 -1
       条件用 >，满足往左
       最后返回 left - 1
```
