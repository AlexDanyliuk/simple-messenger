<template>
  <router-view />
</template>

<script>
import { connect } from "./services/websocket";

export default {
  mounted() {
    connect();
  }
};
</script>

