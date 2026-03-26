import { listChordTypes, listScaleTypes } from './patterns.js';

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

export function listLibraryItems() {
  return [...createLibraryItems('scale', listScaleTypes()), ...createLibraryItems('chord', listChordTypes())];
}

export function searchLibraryItems(query = '', { kind = 'all' } = {}) {
  const normalizedQuery = normalizeSearchText(query);

  return listLibraryItems().filter((item) => {
    if (kind !== 'all' && item.kind !== kind) {
      return false;
    }

    if (!normalizedQuery) {
      return true;
    }

    return normalizeSearchText(item.searchText).includes(normalizedQuery);
  });
}
