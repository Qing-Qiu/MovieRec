<template>
  <div class="skeleton-card">
    <a-skeleton-image class="skeleton-poster" />
    <a-skeleton :paragraph="{ rows: 2 }" active />
  </div>
</template>

<script>
export default {
  name: "SkeletonCard"
}
</script>

<style scoped>
.skeleton-card {
  padding: 12px;
  background: var(--movie-surface);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: var(--movie-shadow-sm);
}

:deep(.skeleton-poster) {
  width: 100% !important;
  height: auto !important;
  aspect-ratio: 2 / 3;
  margin-bottom: 12px;
  border-radius: 6px;
}
</style>
