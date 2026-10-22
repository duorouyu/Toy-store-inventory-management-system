<template>
  <div>
    <div class="page-header">
      <div>
        <div class="page-title">系统设置</div>
        <div class="page-desc">角色权限、个人信息与安全设置</div>
      </div>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="个人信息" name="profile">
        <div class="tp-card" style="max-width: 480px">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="账号">{{ auth.employee?.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ auth.employee?.realName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ auth.employee?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ auth.roleText }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <div class="tp-card" style="max-width: 480px">
          <el-form label-width="90px">
            <el-form-item label="原密码">
              <el-input v-model="pwd.old" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.new" type="password" show-password placeholder="6-32 位" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwd.confirm" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="changePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
          <div class="tip">修改密码需先验证原密码，修改成功后下次登录请使用新密码。</div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="auth.isAdmin" label="角色权限" name="roles">
        <div class="tp-card">
          <el-table :data="roles" stripe>
            <el-table-column prop="name" label="角色名称" width="120" />
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="description" label="描述" min-width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openPerm(row)">配置权限</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 权限树 -->
    <el-dialog v-model="permVisible" title="配置权限" width="500px">
      <el-tree
        ref="treeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedKeys"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" @click="savePerm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllRoles, getPermissionTree, getRolePermissions, assignPermissions } from '@/api/employee'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { Permission, Role } from '@/types'

const auth = useAuthStore()
const tab = ref('profile')
const roles = ref<Role[]>([])
const permTree = ref<Permission[]>([])
const permVisible = ref(false)
const checkedKeys = ref<number[]>([])
const currentRole = ref<Role>()
const treeRef = ref()

const pwd = reactive({ old: '', new: '', confirm: '' })
const pwdSaving = ref(false)

const changePwd = async () => {
  if (!pwd.old) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (pwd.new.length < 6 || pwd.new.length > 32) {
    ElMessage.warning('新密码长度需为 6-32 位')
    return
  }
  if (pwd.new !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  pwdSaving.value = true
  try {
    await changePassword({ oldPassword: pwd.old, newPassword: pwd.new })
    ElMessage.success('密码修改成功')
    pwd.old = ''
    pwd.new = ''
    pwd.confirm = ''
  } finally {
    pwdSaving.value = false
  }
}

const openPerm = async (row: Role) => {
  currentRole.value = row
  checkedKeys.value = await getRolePermissions(row.id)
  permVisible.value = true
}

const savePerm = async () => {
  if (!currentRole.value) return
  const keys = treeRef.value?.getCheckedKeys() as number[]
  await assignPermissions(currentRole.value.id, keys)
  ElMessage.success('权限已更新')
  permVisible.value = false
}

onMounted(async () => {
  if (auth.isAdmin) {
    roles.value = await getAllRoles()
    permTree.value = await getPermissionTree()
  }
})
</script>

<style scoped lang="scss">
.tip {
  font-size: 12px;
  color: var(--tp-text-3);
  margin-top: 8px;
}
</style>
