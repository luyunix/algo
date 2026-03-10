# EF004 - 寻找峰值 题解

## 代码

```java
public int findPeakElement(int[] nums) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] < nums[mid + 1]) {
            left = mid + 1;  // 上坡，往右
        } else {
            right = mid;      // 下坡，往左
        }
    }
    return left;
}
```

## 核心思想

```
比较 nums[mid] 和 nums[mid+1]：

上坡: nums[mid] < nums[mid+1]
     峰值在右边，left = mid + 1
     
下坡: nums[mid] > nums[mid+1]
     峰值在左边（包含mid），right = mid
     
为什么可以和右边比较？
因为题目假设 nums[n] = -∞，所以右边一定会有一个下降
```

## 执行过程图解

### Case 1: 峰值在中间

**输入**: nums = [1, 2, 3, 1]

```
初始: left=0, right=3

第1轮:
[1, 2, 3, 1]
 L     M     R    mid=1, nums[1]=2 < nums[2]=3
       ↑           上坡！峰值在右边
                  left = mid + 1 = 2

第2轮:
[1, 2, 3, 1]
          L=R=3    left == right，结束

返回 left = 2 ✓ (峰值 3)
```

### Case 2: 多个峰值

**输入**: nums = [1, 2, 1, 3, 5, 6, 4]

```
初始: left=0, right=6

第1轮:
[1, 2, 1, 3, 5, 6, 4]
 L        M        R   mid=3, nums[3]=3 < nums[4]=5
          ↑            上坡！left = 4

第2轮:
[1, 2, 1, 3, 5, 6, 4]
                L  M  R  mid=5, nums[5]=6 > nums[6]=4
                   ↑       下坡！right = 5

第3轮:
[1, 2, 1, 3, 5, 6, 4]
                L=R=5    结束

返回 left = 5 ✓ (峰值 6)
```

### Case 3: 单调递增（边界）

**输入**: nums = [1, 2, 3, 4, 5]

```
初始: left=0, right=4

第1轮:
[1, 2, 3, 4, 5]
 L     M     R    mid=2, nums[2]=3 < nums[3]=4
       ↑           上坡！left = 3

第2轮:
[1, 2, 3, 4, 5]
          L  M  R   mid=3, nums[3]=4 < nums[4]=5
             ↑       上坡！left = 4

第3轮:
[1, 2, 3, 4, 5]
             L=R=4   结束

返回 left = 4 ✓ (最后一个元素是峰值，因为右边是-∞)
```

### Case 4: 单调递减（边界）

**输入**: nums = [5, 4, 3, 2, 1]

```
初始: left=0, right=4

第1轮:
[5, 4, 3, 2, 1]
 L     M     R    mid=2, nums[2]=3 > nums[3]=2
       ↑           下坡！right = 2

第2轮:
[5, 4, 3, 2, 1]
 L  M  R          mid=1, nums[1]=4 > nums[2]=3
    ↑              下坡！right = 1

第3轮:
[5, 4, 3, 2, 1]
 L=M=R=0           mid=0, nums[0]=5 > nums[1]=4
 ↑                 下坡！right = 0

第4轮:
[5, 4, 3, 2, 1]
 L=R=0             结束

返回 left = 0 ✓ (第一个元素是峰值，因为左边是-∞)
```

### Case 5: 单元素（边界）

**输入**: nums = [1]

```
初始: left=0, right=0

直接 left == right

返回 0 ✓ (唯一元素就是峰值)
```

## 为什么不会越界？

```
mid = left + (right - left) / 2

当 left < right 时：
- mid 最大为 right - 1（因为整数除法向下取整）
- 所以 mid + 1 <= right，不会越界

当 left == right 时：
- 循环结束，不会访问 nums[mid+1]
```

## 总结

```
找峰值：
- 上坡往右走（left = mid + 1）
- 下坡往左走（right = mid）
- 最后 left == right 就是峰值

关键：和右边邻居比较
```
