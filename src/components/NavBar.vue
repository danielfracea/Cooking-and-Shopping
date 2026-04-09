<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span class="brand-icon">🍳</span>
      <span class="brand-name">Cook &amp; Shop</span>
    </div>
    <div class="navbar-links">
      <RouterLink to="/shopping-lists" class="nav-link">
        <span>🛒</span> Shopping Lists
      </RouterLink>
      <RouterLink to="/ingredients" class="nav-link">
        <span>🥦</span> Ingredients
      </RouterLink>
      <RouterLink to="/recipes" class="nav-link">
        <span>📖</span> Recipes
      </RouterLink>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  background: white;
  border-bottom: 1px solid var(--border);
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 32px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.brand-icon {
  font-size: 1.8rem;
}

.brand-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-dark);
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.nav-link:hover {
  background: var(--background);
  color: var(--text-primary);
}

.nav-link.router-link-active {
  background: var(--primary-light);
  color: var(--primary-dark);
}
</style>
