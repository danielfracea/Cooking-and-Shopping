<template>
  <div class="app">
    <NavBar />
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>
