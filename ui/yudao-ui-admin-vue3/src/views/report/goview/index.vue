<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script lang="ts" setup>
defineOptions({ name: 'GoView' })

const src = 'http://10.109.74.23:3000'
</script>
