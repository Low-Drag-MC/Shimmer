package com.lowdragmc.shimmer.client.shader;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.lwjgl.opengl.GL43;


/**
 * utils for query OpenGL information
 */
public class Query {

    public static int queryUniformBlockIndex(int programId,String uniformBlockName){
        return GL43.glGetUniformBlockIndex(programId,uniformBlockName);
    }

    /**
     * query the uniform buffer object's memory layout
     */
    public static Object2IntMap<String> queryUniformOffsetInUniformBufferOffset(int programID, int uboIndex) {
        Object2IntMap<String> map = new Object2IntArrayMap<>();
        int[] propNumberActiveVariable = new int[]{GL43.GL_NUM_ACTIVE_VARIABLES};
        int[] propActiveVariable = new int[]{GL43.GL_ACTIVE_VARIABLES};
        int[] propOffset = new int[]{GL43.GL_OFFSET};
        int[] activeUniforms = new int[1];
        int[] lengthOne = new int[]{1};
        GL43.glGetProgramResourceiv(programID, GL43.GL_UNIFORM_BLOCK, uboIndex, propNumberActiveVariable, lengthOne, activeUniforms);
        int data[] = new int[activeUniforms[0]];
        GL43.glGetProgramResourceiv(programID, GL43.GL_UNIFORM_BLOCK, uboIndex, propActiveVariable, lengthOne, data);
        int[] offsetData = new int[1];
        for (int uniformIndex = 0; uniformIndex < activeUniforms[0]; ++uniformIndex) {
            String name = GL43.glGetProgramResourceName(programID, GL43.GL_UNIFORM, data[uniformIndex]);
            GL43.glGetProgramResourceiv(programID, GL43.GL_UNIFORM, data[uniformIndex], propOffset, lengthOne, offsetData);
            map.put(name, offsetData[0]);
        }
        return map;
    }
}