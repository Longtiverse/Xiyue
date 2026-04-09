// Storage utility for persisting user settings
const STORAGE_KEY = 'xiyue-settings';
const FAVORITES_KEY = 'xiyue-favorites';
const HISTORY_KEY = 'xiyue-history';
const MAX_HISTORY_ITEMS = 20;

export const storage = {
  // Settings
  getSettings() {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },

  saveSettings(settings) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
      return true;
    } catch {
      return false;
    }
  },

  // Favorites
  getFavorites() {
    try {
      const data = localStorage.getItem(FAVORITES_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  saveFavorites(favorites) {
    try {
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
      return true;
    } catch {
      return false;
    }
  },

  addFavorite(itemId) {
    const favorites = this.getFavorites();
    if (!favorites.includes(itemId)) {
      favorites.push(itemId);
      this.saveFavorites(favorites);
    }
  },

  removeFavorite(itemId) {
    const favorites = this.getFavorites();
    const index = favorites.indexOf(itemId);
    if (index > -1) {
      favorites.splice(index, 1);
      this.saveFavorites(favorites);
    }
  },

  isFavorite(itemId) {
    return this.getFavorites().includes(itemId);
  },

  // History (Recent items)
  getHistory() {
    try {
      const data = localStorage.getItem(HISTORY_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  saveHistory(history) {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(0, MAX_HISTORY_ITEMS)));
      return true;
    } catch {
      return false;
    }
  },

  addToHistory(itemId) {
    const history = this.getHistory();
    // Remove if already exists (to move to front)
    const index = history.indexOf(itemId);
    if (index > -1) {
      history.splice(index, 1);
    }
    // Add to front
    history.unshift(itemId);
    // Keep only recent items
    this.saveHistory(history.slice(0, MAX_HISTORY_ITEMS));
  },

  // Import/Export
  exportData() {
    return JSON.stringify({
      settings: this.getSettings(),
      favorites: this.getFavorites(),
      history: this.getHistory(),
    });
  },

  importData(jsonString) {
    try {
      const data = JSON.parse(jsonString);
      if (data.settings) this.saveSettings(data.settings);
      if (data.favorites) this.saveFavorites(data.favorites);
      if (data.history) this.saveHistory(data.history);
      return true;
    } catch {
      return false;
    }
  },

  // Clear all data
  clearAll() {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(FAVORITES_KEY);
    localStorage.removeItem(HISTORY_KEY);
  },
};

export default storage;
