# ວິທີເອົາ Web App ນີ້ຂຶ້ນ Cloudflare Pages (ຟຣີ 100%)

ໂຟນເດີ `webapp` ນີ້ເປັນລະບົບ **Web App (PWA) ລາຍຮັບ-ລາຍຈ່າຍ** ແບບ Static ທີ່ພ້ອມນຳໃຊ້ງານທັນທີ ໂດຍບໍ່ຕ້ອງຕິດຕັ້ງ Node.js ຫຼື Build ເຄື່ອງມືໃດໆເພີ່ມເຕີມ!

---

### ວິທີທີ 1: ລາກໂຟນເດີລົງ Cloudflare Pages ໂດຍກົງ (Direct Upload - ໄວທີ່ສຸດ 30 ວິນາທີ)

1. ເຂົ້າສູ່ລະບົບ **[Cloudflare Dashboard](https://dash.cloudflare.com/)** (ສະໝັກຟຣີ)
2. ໄປທີ່ເມນູ **Workers & Pages** ຢູ່ແຖບດ້ານຊ້າຍ
3. ກົດປຸ່ມ **Create application** -> ເລືອກແທັບ **Pages** -> ເລືອກ **Upload assets**
4. ຕັ້ງຊື່ໂຄງການ (Project Name) ເຊັ່ນ: `lao-budget`
5. **ລາກໂຟນເດີ `webapp`** ນີ້ໄປວາງໃສ່ (ຫຼື Zip ໂຟນເດີ `webapp` ແລ້ວອັບໂຫຼດ)
6. ກົດ **Deploy site**
7. ສຳເລັດແລ້ວ! ທ່ານຈະໄດ້ລິ້ງເວັບໄຊເຊັ່ນ:
   👉 **`https://lao-budget.pages.dev`** ສາມາດແຊຣ໌ໃຫ້ໝູ່ ຫຼື ເປີດໃຊ້ງານໄດ້ທັນທີ!

---

### ວິທີທີ 2: ເຊື່ອມຕໍ່ຜ່ານ GitHub (ອັບເດດອັດຕະໂນມັດ)

1. Push ໂຄງການນີ້ຂຶ້ນ GitHub ຂອງທ່ານ
2. ໃນ Cloudflare Pages -> ເລືອກ **Connect to Git** -> ເລືອກ repository ຂອງທ່ານ
3. ຕັ້ງຄ່າ:
   - **Framework preset**: None
   - **Build output directory**: `webapp`
4. ກົດ **Save and Deploy**
