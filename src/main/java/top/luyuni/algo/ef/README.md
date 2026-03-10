# EF 目录 - 易错算法专题

## 二分查找万能模板

```java
// 找满足条件的最小值（第一个满足条件的）
int left = 最小可能值, right = 最大可能值 + 1;
while (left < right) {
    int mid = left + (right - left) / 2;
    if (check(mid)) right = mid;
    else left = mid + 1;
}
return left;

// 找满足条件的最大值（最后一个满足条件的）
int left = 最小可能值, right = 最大可能值 + 1;
while (left < right) {
    int mid = left + (right - left) / 2;
    if (check(mid)) left = mid + 1;
    else right = mid;
}
return left - 1;
```

## 旋转数组最小值（无重复）

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

## 旋转数组最小值（有重复）

```java
public int findMin(int[] nums) {
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
