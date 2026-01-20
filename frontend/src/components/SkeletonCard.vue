<template>
  <div class="skeleton-card">
    <a-skeleton-image style="width: 100%; height: 260px; margin-bottom: 8px" />
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
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>
