import { listChordTypes, listScaleTypes } from './patterns.js';

// ============================================
// 模块级缓存变量
// ============================================
let cachedLibraryItems = null;
let searchIndex = null;

function createLibraryItems(kind, types) {
  return types.map((type) => ({
    id: `${kind}:${type}`,
    kind,
    type,
    label: type,
    searchText: `${kind} ${type}`.replace(/([a-z])([A-Z])/g, '$1 $2').toLowerCase(),
  }));
}

function normalizeSearchText(value = '') {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, '');
}

/**
 * 构建倒排索引（支持任意位置子字符串）
 * 将每个词元映射到包含该词元的项目ID列表
 */
function buildSearchIndex(items) {
  const index = new Map();
  const itemMap = new Map();

  for (const item of items) {
    itemMap.set(item.id, item);

    // 从searchText中提取词元
    const normalized = normalizeSearchText(item.searchText);

    // 为所有可能的子字符串建立索引
    for (let i = 0; i < normalized.length; i++) {
      for (let j = i + 1; j <= normalized.length; j++) {
        const token = normalized.slice(i, j);
        if (!index.has(token)) {
          index.set(token, new Set());
        }
        index.get(token).add(item.id);
      }
    }
  }

  return { index, itemMap };
}

/**
 * 获取缓存的库项目列表（首次调用时构建）
 */
export function listLibraryItems() {
  if (!cachedLibraryItems) {
    cachedLibraryItems = [
      ...createLibraryItems('scale', listScaleTypes()),
      ...createLibraryItems('chord', listChordTypes()),
    ];
  }
  return [...cachedLibraryItems];
}

/**
 * 获取搜索索引（首次调用时构建）
 */
function getSearchIndex() {
  if (!searchIndex) {
    searchIndex = buildSearchIndex(listLibraryItems());
  }
  return searchIndex;
}

/**
 * 清除缓存（用于测试或热更新场景）
 */
export function clearLibraryCache() {
  cachedLibraryItems = null;
  searchIndex = null;
}

/**
 * 使用倒排索引优化搜索库项目
 *
 * 优化策略：
 * 1. 使用完整子字符串索引直接获取匹配的项目
 * 2. 无需在运行时进行全量扫描
 */
export function searchLibraryItems(query = '', { kind = 'all' } = {}) {
  const normalizedQuery = normalizeSearchText(query);

  // 获取所有符合条件的项目（使用索引或全量）
  let results;

  if (normalizedQuery) {
    // 使用倒排索引直接查找匹配的项目
    const { index, itemMap } = getSearchIndex();
    const candidateIds = index.get(normalizedQuery);

    if (!candidateIds || candidateIds.size === 0) {
      return [];
    }

    // 将候选ID转换为项目对象
    results = Array.from(candidateIds).map((id) => itemMap.get(id));
  } else {
    results = listLibraryItems();
  }

  // 应用 kind 过滤
  if (kind !== 'all') {
    results = results.filter((item) => item.kind === kind);
  }

  return results;
}
