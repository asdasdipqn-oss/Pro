<template>
  <div class="candidate-dashboard">
    <div class="welcome-banner">
      <h1>欢迎，求职者！</h1>
      <p>功能开发中，敬请期待...</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.candidate-dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F5F5F7;
}

.welcome-banner {
  text-align: center;
  background: #FFFFFF;
  padding: 80px 120px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
}

.welcome-banner h1 {
  font-size: 32px;
  color: #1D1D1F;
  margin-bottom: 16px;
}

.welcome-banner p {
  font-size: 18px;
  color: #86868B;
}
</style>
